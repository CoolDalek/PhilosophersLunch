import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Files
import java.util.zip.{ZipEntry, ZipOutputStream}

import scala.util.control.NonFatal

object ZipProject {

  def apply(name: String): Unit =
    Using.Manager { use =>
      val projectName = Shared.projectName(name)
      val fileStream = use(new FileOutputStream(s"${Shared.universalPath}/$projectName.zip"))
      val zipStream = use(new ZipOutputStream(fileStream))
      val source = new File(Shared.stagePath)

      def zipFile(file: File, fileName: String): Unit =
        if(!Files.isHidden(file.toPath)) {

          if(Files.isDirectory(file.toPath)) {
            val dirName = {
              val candidate = fileName
              if(candidate.endsWith("/")) candidate else s"$candidate/"
            }
            zipStream.putNextEntry(new ZipEntry(dirName))
            zipStream.closeEntry()
            val children = file.listFiles()
            children.foreach { children =>
              zipFile(children, s"$dirName${children.getName}")
            }
          } else {
            Using(new FileInputStream(file)) { input =>
              zipStream.putNextEntry(new ZipEntry(fileName))
              val buffer = new Array[Byte](1024)

              @scala.annotation.tailrec
              def write(): Unit = {
                val length = input.read(buffer)
                if (length >= 0) {
                  zipStream.write(buffer, 0, length)
                  write()
                }
              }

              write()
            }.recover {
              case NonFatal(exc) =>
                println(s"Exception during archiving $fileName")
                throw exc
            }.get
          }

        }

      source.listFiles().foreach { child =>
        zipFile(child, child.getName)
      }

    }.recover {
      case NonFatal(exc) =>
        println("Exception during zipping project")
        exc.printStackTrace()
        throw exc
    }.get


}