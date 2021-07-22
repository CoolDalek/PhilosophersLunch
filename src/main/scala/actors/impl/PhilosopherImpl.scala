package actors.impl

import actors.{Philosopher, Scheduler}
import actors.Philosopher._
import actors.Scheduler.SchedulerActor
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration._

class PhilosopherImpl extends Philosopher {

  override def apply(schedulerActor: SchedulerActor, number: Int): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Starting philosopher $number")
      Behaviors.withTimers { timers =>
        Behaviors.receiveMessage {

          case Acquire(left, right) =>
            ctx.log.info(s"Acquired $left, $right")
            timers.startSingleTimer(Release(left, right), 5 seconds)
            Behaviors.same

          case Release(left, right) =>
            ctx.log.info(s"Releasing $left, $right")
            schedulerActor ! Scheduler.Release(left, right)
            Behaviors.same
        }
      }
    }

}