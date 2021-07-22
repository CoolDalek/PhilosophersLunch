package models

case class Fork(
                 self: Int,
                 next: Int,
               )
object Fork {

  class Availability(val fork: Fork) {

    var isAvailable: Boolean = true

    def next: Int = fork.next

    override def toString: String =
      s"$fork, $isAvailable"

  }

  implicit val ordering: Ordering[Fork] = Ordering.by(_.self)

  def build(total: Int): Vector[Availability] =
    Vector.tabulate(total) { i =>
      val next = {
        val candidate = i + 1
        if(candidate == total) 0 else candidate
      }
      new Availability(
        Fork(
          self = i,
          next = next,
        )
      )
    }

}