package configs

import scala.concurrent.duration.FiniteDuration

case class WorkersConfig(
                          workingTime: FiniteDuration,
                          workersNumber: Int,
                        )