package models

sealed trait Priority

object Priority {

  case object High extends Priority

  case object Low extends Priority

}