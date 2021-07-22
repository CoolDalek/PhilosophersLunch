package models

class Shared[T](
                 val resource: T,
                 var isAvailable: Boolean = true,
               ) {

  override def toString: String =
    s"Shared($resource,$isAvailable)"

  override def hashCode(): Int = resource.hashCode()

  override def equals(obj: Any): Boolean =
    obj match {
      case Shared(resource, isAvailable) =>
          resource == resource && isAvailable == isAvailable
      case _ =>
        false
    }

}
object Shared {

  def apply[T](resource: T, isAvailable: Boolean = true): Shared[T] =
    new Shared(resource, isAvailable)

  def unapply[T](any: Any): Option[(T, Boolean)] =
    any match {
      case shared: Shared[T] =>
        Some(shared.resource, shared.isAvailable)
      case _ =>
        None
    }

}