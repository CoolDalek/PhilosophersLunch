import actors.{Worker, Scheduler}
import actors.impl.{WorkerImpl, SchedulerImpl}
import akka.actor.typed.ActorSystem
import configs.WorkersConfig
import pureconfig._
import pureconfig.generic.auto._

trait Module {

  implicit val configSource: ConfigObjectSource = ConfigSource.default

  implicit val philosophersConf: WorkersConfig = configSource.loadOrThrow[WorkersConfig]

  val worker: Worker = new WorkerImpl

  val scheduler: Scheduler = new SchedulerImpl(worker)

  implicit val system: ActorSystem[Scheduler.Protocol] =
    ActorSystem(scheduler(), "PhilosopherLunch")

  sys.addShutdownHook(system.terminate())

}