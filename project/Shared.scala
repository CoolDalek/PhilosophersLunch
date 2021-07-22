object Shared {

  lazy val universalPath: String = "target/universal"

  lazy val stagePath: String = s"$universalPath/stage"

  lazy val binPath: String = s"$stagePath/bin"

  def projectName(name: String): String = name.map(_.toLower)

}