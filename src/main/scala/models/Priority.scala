package models

import models.Priority._

sealed trait Priority {

  def increase: Priority = High

  def decrease: Priority = Low

  def reverse: Priority = if(isHigh) Low else High

  def isHigh: Boolean = this == High

  def isLow: Boolean = this == Low

}

object Priority {

  case object High extends Priority

  case object Low extends Priority

}