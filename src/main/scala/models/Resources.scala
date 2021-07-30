package models

class Resources private(val underlay: Seq[Shared[Resource]]) {

  private def getWithNext(idx: Int): (Shared[Resource], Shared[Resource]) = {
    val left = underlay(idx)
    val right = underlay(left.resource.next)
    (left, right)
  }

  private def tapWithNext(idx: Int)
                         (body: (Shared[Resource], Shared[Resource]) => Unit): (Shared[Resource], Shared[Resource]) = {
    val (left, right) = getWithNext(idx)
    body(left, right)
    (left, right)
  }

  private def tap(idx: Int)(body: Shared[Resource] => Unit): Shared[Resource] = {
    val resource = underlay(idx)
    body(resource)
    resource
  }

  def isAvailableWithNext(idx: Int): Boolean = {
    val (left, right) = getWithNext(idx)
    left.isAvailable && right.isAvailable
  }

  def release(idx: Int): Shared[Resource] =
    tap(idx)(_.setAvailable())

  def acquireWithNext(idx: Int): (Shared[Resource], Shared[Resource]) =
    tapWithNext(idx) { (left, right) =>
      left.setUnavailable()
      right.setUnavailable()
    }

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