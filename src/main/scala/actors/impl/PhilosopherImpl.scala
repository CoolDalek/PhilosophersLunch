package actors.impl

import actors.{Philosopher, Scheduler}
import actors.Philosopher._
import actors.Scheduler.SchedulerActor
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import configs.PhilosophersConfig

import scala.concurrent.duration._

class PhilosopherImpl(implicit config: PhilosophersConfig) extends Philosopher {

  override def apply(schedulerActor: SchedulerActor, number: Int): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Starting philosopher $number")
      Behaviors.withTimers { timers =>
        Behaviors.receiveMessage {

          case Acquire(left, right) =>
            ctx.log.info(s"Acquired $left, $right by philosopher $number")
            timers.startSingleTimer(Release(left, right), config.eatingTime)
            Behaviors.same

          case Release(left, right) =>
            ctx.log.info(s"Releasing $left, $right")
            schedulerActor ! Scheduler.Release(left, right)
            Behaviors.same
        }
      }
    }

}