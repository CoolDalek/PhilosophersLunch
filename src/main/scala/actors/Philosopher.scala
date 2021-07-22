package actors

import actors.Philosopher.Protocol
import actors.Scheduler.SchedulerActor
import akka.actor.typed.{ActorRef, Behavior}
import models.Fork

trait Philosopher {

  def apply(scheduler: SchedulerActor, number: Int): Behavior[Protocol]
  
}
object Philosopher {

  type PhilosopherActor = ActorRef[Protocol]

  sealed trait Protocol

  case class Acquire(left: Fork, right: Fork) extends Protocol

  case class Release(left: Fork, right: Fork) extends Protocol

}