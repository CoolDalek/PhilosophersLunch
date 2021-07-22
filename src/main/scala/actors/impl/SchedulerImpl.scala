package actors.impl

import actors.Philosopher.PhilosopherActor
import actors.Scheduler._
import actors.{Philosopher, _}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import models.Fork
import models.Fork.Availability

import scala.collection.mutable

class SchedulerImpl(philosopher: Philosopher) extends Scheduler {

  type WorkingQueue = mutable.Queue[Int]
  type Forks = Vector[Availability]
  type Philosophers = Vector[PhilosopherActor]

  def setup(total: Int)(implicit ctx: ActorContext[Protocol]): (WorkingQueue, Forks, Philosophers) = {
    def spawnPhilosopher(idx: Int)(implicit ctx: ActorContext[Protocol]): PhilosopherActor =
      ctx.spawn(
        behavior = philosopher(
          scheduler = ctx.self,
          number = idx
        ),
        name = s"Philosopher-$idx",
      )

    val workingQueue = mutable.Queue.tabulate(total)(identity)
    val philosophers = Vector.newBuilder[PhilosopherActor]
    workingQueue.foreach { i =>
      philosophers.addOne(spawnPhilosopher(i))
    }

    (workingQueue, Fork.build(total), philosophers.result())
  }

  def schedule(implicit queue: WorkingQueue,
               forks: Forks,
               philosophers: Philosophers,
               ctx: ActorContext[Protocol]): Unit = {
    ctx.log.info("Scheduling work")

    def isAvailable(i: Int) = {
      val fork = forks(i)
      fork.isAvailable && forks(fork.next).isAvailable
    }

    def acquire(i: Int): Philosopher.Acquire = {
      val left = forks(i)
      val right = forks(left.next)
      ctx.log.info(s"Acquire $left and $right")
      left.isAvailable = false
      right.isAvailable = false
      queue.subtractOne(i)
      queue.enqueue(i)
      Philosopher.Acquire(left.fork, right.fork)
    }

    def inWork(): Unit =
      queue.view.find(isAvailable).foreach { i =>
        ctx.log.info(s"Philosopher $i is available.")
        philosophers(i) ! acquire(i)
        inWork()
    }

    inWork()
  }

  override def apply(philosophersNumber: Int): Behavior[Protocol] =
    Behaviors.setup { implicit ctx =>
      ctx.log.info("Scheduler starting")
      val vars = setup(philosophersNumber)
      implicit val queue: WorkingQueue = vars._1
      implicit val forks: Forks = vars._2
      implicit val philosophers: Philosophers = vars._3
      ctx.log.info("Scheduler setup completed")
      schedule
      working
    }

  def release(left: Fork, right: Fork)
             (implicit forks: Forks, ctx: ActorContext[Protocol]): Unit = {
    forks(left.self).isAvailable = true
    forks(right.self).isAvailable = true
    ctx.log.info(s"Released $left, $right")
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

}