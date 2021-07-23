package models

import actors.Worker.WorkerActor
import models.Priority._

case class WorkerCell(number: Int, actor: WorkerActor) {

  private var priority: Priority = High

  private[models] def reverse(): Unit = priority = priority.reverse

  private[models] def setHigh(): Unit = priority = High

  private[models] def setLow(): Unit = priority = Low

  def isHigh: Boolean = priority.isHigh

  def isLow: Boolean = priority.isLow

  override def toString: String =
    s"WorkerCell($number, $actor, $priority)"

}