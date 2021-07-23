package actors.impl

import actors.Worker._
import actors.Scheduler.SchedulerActor
import actors.{Worker, Scheduler}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import configs.WorkersConfig

class WorkerImpl(implicit config: WorkersConfig) extends Worker {

  override def apply(schedulerActor: SchedulerActor, number: Int): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Starting worker $number")
      Behaviors.withTimers { timers =>
        Behaviors.receiveMessage {

          case Acquire(left, right) =>
            ctx.log.info(s"Acquired $left, $right by worker $number")
            timers.startSingleTimer(Release(left, right), config.workingTime)
            Behaviors.same

          case Release(left, right) =>
            ctx.log.info(s"Releasing $left, $right")
            schedulerActor ! Scheduler.Release(left, right)
            Behaviors.same
        }
      }
    }

}