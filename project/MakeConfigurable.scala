import java.io.PrintWriter
import java.nio.file.{Files, Path, StandardCopyOption}

import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal

object MakeConfigurable {

  def apply(name: String): Unit = {
    val shName = Shared.projectName(name)
    val batName = s"$shName.bat"
    val configFile = "application.conf"
    val arg = s"""-Dconfig.file="../$configFile""""

    def addArgs(fileName: String, terminalLine: String, appendLine: String): Try[Unit] = {
      Using.Manager { use =>
        def terminal(test: String): Boolean = !test.contains(terminalLine)

        val file = s"${Shared.binPath}/$fileName"
        val tmp = s"$file.tmp"

        def lines: Iterator[String] = use(Source.fromFile(file)).getLines()

        val sink = use(new PrintWriter(tmp))

        val linesBefore = lines.takeWhile(terminal)
        val linesAfter = lines.dropWhile(terminal)

        linesBefore.foreach(sink.println)
        sink.println(linesAfter.next())
        sink.println(appendLine)
        linesAfter.foreach(sink.println)

        Files.delete(Path.of(file))
        Files.move(Path.of(tmp), Path.of(file), StandardCopyOption.REPLACE_EXISTING)
        Files.deleteIfExists(Path.of(tmp))
      }
    }

    def addConf() = {
      val projectConf = Path.of("src", "main", "resources", configFile)
      val buildConf = Path.of(Shared.stagePath, configFile)
      Files.copy(projectConf, buildConf, StandardCopyOption.REPLACE_EXISTING)
    }

    for {
      _ <- addArgs(shName, "run() {", s"  addApp $arg")
      _ <- addArgs(batName, "set _APP_ARGS=", s"call :add_app $arg")
    } yield addConf()

  }.recover {
    case NonFatal(exc) =>
      println("Exception during making build configurable")
      exc.printStackTrace()
      throw exc
  }.get

}