package actors

import actors.Scheduler.Protocol
import akka.actor.typed.{ActorRef, Behavior}
import models.Fork

trait Scheduler {

  def apply(philosophersNumber: Int): Behavior[Protocol]

}
object Scheduler {

  type SchedulerActor = ActorRef[Protocol]

  sealed trait Protocol

  case class Release(left: Fork, right: Fork) extends Protocol

}