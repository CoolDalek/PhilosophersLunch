package models

import models.Priority._

case class Worker(number: Int) {

  var priority: Priority = High

  def setHigh(): Unit = priority = High

  def setLow(): Unit = priority = Low

  def isHigh: Boolean = priority.isHigh

  def isLow: Boolean = priority.isLow

  override def toString: String =
    s"Worker($number, $priority)"

}