import actors.{Philosopher, Scheduler}
import actors.impl.{PhilosopherImpl, SchedulerImpl}
import akka.actor.typed.ActorSystem
import configs.PhilosophersConfig
import pureconfig._
import pureconfig.generic.auto._

trait Module {

  implicit val configSource: ConfigObjectSource = ConfigSource.default

  implicit val philosophersConf: PhilosophersConfig = configSource.loadOrThrow[PhilosophersConfig]

  val philosopher: Philosopher = new PhilosopherImpl

  val scheduler: Scheduler = new SchedulerImpl(philosopher)

  implicit val system: ActorSystem[Scheduler.Protocol] =
    ActorSystem(scheduler(), "PhilosopherLunch")

  sys.addShutdownHook(system.terminate())

}