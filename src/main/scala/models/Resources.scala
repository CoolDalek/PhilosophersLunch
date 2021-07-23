package models

class Resources private(val underlay: Vector[Shared[Resource]]) {

  private def get(idx: Int): (Shared[Resource], Shared[Resource]) = {
    val left = underlay(idx)
    val right = underlay(left.resource.next)
    (left, right)
  }

  private def tapComplete(idx: Int)
                         (body: (Shared[Resource], Shared[Resource]) => Unit): (Shared[Resource], Shared[Resource]) = {
    val (left, right) = get(idx)
    body(left, right)
    (left, right)
  }

  private def tapOne(idx: Int)(body: Shared[Resource] => Unit): Shared[Resource] = {
    val resource = underlay(idx)
    body(resource)
    resource
  }

  def isAvailableComplete(idx: Int): Boolean = {
    val (left, right) = get(idx)
    left.isAvailable && right.isAvailable
  }

  def isAvailableOne(idx: Int): Boolean =
    underlay(idx).isAvailable

  def releaseComplete(idx: Int): (Shared[Resource], Shared[Resource]) =
    tapComplete(idx) { (left, right) =>
      left.setAvailable()
      right.setAvailable()
    }

  def releaseOne(idx: Int): Shared[Resource] =
    tapOne(idx)(_.setAvailable())

  def acquireComplete(idx: Int): (Shared[Resource], Shared[Resource]) =
    tapComplete(idx) { (left, right) =>
      left.setUnavailable()
      right.setUnavailable()
    }

  def acquireOne(idx: Int): Shared[Resource] =
    tapOne(idx)(_.setUnavailable())

}
object Resources {

  def apply(total: Int): Resources = {
    val builder = Vector.newBuilder[Shared[Resource]]
    var i = 0
    while(i < total) {
      builder.addOne(Shared(Resource.create(i, total)))
      i += 1
    }
    new Resources(builder.result())
  }

}