package actors.impl

import actors.Scheduler._
import actors.{Worker, _}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import configs.WorkersConfig
import models._

class SchedulerImpl(worker: Worker)
                   (implicit conf: WorkersConfig) extends Scheduler {

  override def apply(): Behavior[Protocol] =
    Behaviors.setup { implicit ctx =>
      ctx.log.info("Scheduler starting")
      implicit val (workingQueue, resources) = setup
      ctx.log.info("Scheduler setup completed")
      schedule
      working
    }

  def working(implicit ctx: ActorContext[Protocol],
              workingQueue: WorkingQueue,
              resources: Resources): Behavior[Protocol] =
    Behaviors.receiveMessage {
      case Release(left, right) =>
        release(left, right)
        schedule
        Behaviors.same
    }

  def setup(implicit ctx: ActorContext[Protocol]): (WorkingQueue, Resources) = {
    val total = conf.workersNumber

    val workingQueue = WorkingQueue(total) { idx =>
      ctx.spawn(
        behavior = worker(
          scheduler = ctx.self,
          number = idx
        ),
        name = s"Worker-$idx",
      )
    }
    val resources = Resources(total)

    (workingQueue, resources)
  }

  def schedule(implicit ctx: ActorContext[Protocol],
               workingQueue: WorkingQueue,
               resources: Resources): Unit = {
    ctx.log.info("Scheduling work")

    def isAvailable(worker: WorkerCell) =
      worker.isHigh && resources.isAvailableComplete(worker.number)

    def acquire(i: Int): Worker.Acquire = {
      val (left, right) = resources.acquireComplete(i)
      Worker.Acquire(left.resource, right.resource)
    }

    @scala.annotation.tailrec
    def inWork(position: Int = 0): Unit = {
      val worker = workingQueue(position)

      if(isAvailable(worker)) {

        ctx.log.info(s"Worker $worker is available.")
        worker.actor ! acquire(worker.number)
        workingQueue.reset(worker, position)
        inWork()

      } else {

        val next = position + 1
        if(next < workingQueue.length) {
          inWork(next)
        }

      }
    }

    inWork()
  }

  def release(left: Resource,
              right: Resource)
             (implicit ctx: ActorContext[Protocol],
              workingQueue: WorkingQueue,
              resources: Resources): Unit = {
    resources.releaseOne(left.self)
    resources.releaseOne(right.self)
    ctx.log.info(s"Released $left, $right")
  }

}