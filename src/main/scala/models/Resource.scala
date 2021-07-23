package models

case class Resource(
                     self: Int,
                     next: Int,
                   )
object Resource {

  def create(current: Int, total: Int): Resource = {
    val next = {
      val candidate = current + 1
      if(candidate == total) 0 else candidate
    }
    Resource(current, next)
  }

}