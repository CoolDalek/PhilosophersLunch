package models

case class Shared[T](resource: T) {

  var isAvailable: Boolean = true

  def setAvailable(): Unit = isAvailable = true

  def setUnavailable(): Unit = isAvailable = false

}