package actors

import actors.Scheduler.Protocol
import akka.actor.typed.{ActorRef, Behavior}
import models.Resource

trait Scheduler {

  def apply(): Behavior[Protocol]

}
object Scheduler {

  type SchedulerActor = ActorRef[Protocol]

  sealed trait Protocol

  case class Release(left: Resource, right: Resource) extends Protocol

}