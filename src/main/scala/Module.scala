import actors.{Philosopher, Scheduler}
import actors.impl.{PhilosopherImpl, SchedulerImpl}
import akka.actor.typed.ActorSystem

trait Module {

  val philosopherNumber = 5

  val philosopher: Philosopher = new PhilosopherImpl

  val scheduler: Scheduler = new SchedulerImpl(philosopher)

  implicit val system: ActorSystem[Scheduler.Protocol] =
    ActorSystem(scheduler(philosopherNumber), "PhilosopherLunch")

}