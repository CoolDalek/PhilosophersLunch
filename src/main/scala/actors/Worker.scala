package actors

import actors.Worker.Protocol
import actors.Scheduler.SchedulerActor
import akka.actor.typed.{ActorRef, Behavior}
import models.Resource

trait Worker {

  def apply(scheduler: SchedulerActor, number: Int): Behavior[Protocol]

}
object Worker {

  type WorkerActor = ActorRef[Protocol]

  sealed trait Protocol

  case class Acquire(left: Resource, right: Resource) extends Protocol

  case class Release(left: Resource, right: Resource) extends Protocol

}