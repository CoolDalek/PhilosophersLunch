package models

import scala.reflect._

class Shared[T: ClassTag](
                           val resource: T,
                           var isAvailable: Boolean = true,
                         ) {

  private val resourceClass: Class[_] = classTag[T].runtimeClass

  private def isResourceType[R: ClassTag]: Boolean =
    classTag[R].runtimeClass == resourceClass

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

  def apply[T: ClassTag](resource: T, isAvailable: Boolean = true): Shared[T] =
    new Shared(resource, isAvailable)


  @unchecked
  def unapply[T: ClassTag](any: Any): Option[(T, Boolean)] =
    any match {
      case shared: Shared[_] if shared.isResourceType[T] =>
        Some(shared.resource.asInstanceOf[T], shared.isAvailable)
      case _ =>
        None
    }

}