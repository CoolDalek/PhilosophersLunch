package actors.impl

import actors.Philosopher.PhilosopherActor
import actors.Scheduler._
import actors.{Philosopher, _}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import configs.PhilosophersConfig
import models.{Fork, Shared}

import scala.collection.mutable

class SchedulerImpl(philosopher: Philosopher)
                   (implicit conf: PhilosophersConfig) extends Scheduler {

  type WorkingQueue = mutable.Queue[Int]
  type Forks = Vector[Shared[Fork]]
  type Philosophers = Vector[PhilosopherActor]

  override def apply(): Behavior[Protocol] =
    Behaviors.setup { implicit ctx =>
      ctx.log.info("Scheduler starting")
      val vars = setup
      implicit val queue: WorkingQueue = vars._1
      implicit val forks: Forks = vars._2
      implicit val philosophers: Philosophers = vars._3
      ctx.log.info("Scheduler setup completed")
      schedule
      working
    }

  def working(implicit ctx: ActorContext[Protocol],
              queue: WorkingQueue,
              forks: Forks,
              philosophers: Philosophers): Behavior[Protocol] =
    Behaviors.receiveMessage {
      case Release(left, right) =>
        release(left, right)
        schedule
        Behaviors.same
    }

  def setup(implicit ctx: ActorContext[Protocol]): (WorkingQueue, Forks, Philosophers) = {
    val total = conf.philosophersNumber
    val workingQueue = mutable.Queue.tabulate(total)(identity)
    val philosophers = Vector.newBuilder[PhilosopherActor]
    val forks = Vector.newBuilder[Shared[Fork]]

    def spawnPhilosopher(idx: Int): PhilosopherActor =
      ctx.spawn(
        behavior = philosopher(
          scheduler = ctx.self,
          number = idx
        ),
        name = s"Philosopher-$idx",
      )

    def createFork(current: Int): Shared[Fork] = {
      val next = {
        val candidate = current + 1
        if(candidate == total) 0 else candidate
      }
      Shared(Fork(current, next))
    }

    workingQueue.foreach { i =>
      philosophers.addOne(spawnPhilosopher(i))
      forks.addOne(createFork(i))
    }

    (workingQueue, forks.result(), philosophers.result())
  }

  def schedule(implicit queue: WorkingQueue,
               forks: Forks,
               philosophers: Philosophers,
               ctx: ActorContext[Protocol]): Unit = {
    ctx.log.info("Scheduling work")

    def isAvailable(i: Int) = {
      val fork = forks(i)
      fork.isAvailable && forks(fork.resource.next).isAvailable
    }

    def acquire(i: Int): Philosopher.Acquire = {
      val left = forks(i)
      val right = forks(left.resource.next)

      ctx.log.info(s"Acquire $left and $right")

      left.isAvailable = false
      right.isAvailable = false

      Philosopher.Acquire(left.resource, right.resource)
    }

    @scala.annotation.tailrec
    def inWork(position: Int = 0): Unit = {
      val workerNumber = queue(position)

      if(isAvailable(workerNumber)) {
        ctx.log.info(s"Philosopher $workerNumber is available.")
        philosophers(workerNumber) ! acquire(workerNumber)

        queue.remove(position)
        queue.enqueue(workerNumber)

        inWork()
      } else {

        val next = position + 1
        if(next < queue.length) {
          inWork(next)
        }

      }
    }

    inWork()
  }

  def release(left: Fork,
              right: Fork)
             (implicit forks: Forks,
              ctx: ActorContext[Protocol]): Unit = {
    forks(left.self).isAvailable = true
    forks(right.self).isAvailable = true
    ctx.log.info(s"Released $left, $right")
  }

}