package configs

import scala.concurrent.duration.FiniteDuration

case class PhilosophersConfig(
                               eatingTime: FiniteDuration,
                               philosophersNumber: Int,
                             )