package models

import actors.Worker.WorkerActor

import scala.collection.mutable

class WorkingQueue private(val underlay: mutable.Queue[WorkerCell]) {

  private var lowPriorityCount = 0

  def apply(idx: Int): WorkerCell = underlay(idx)

  def length: Int = underlay.length

  def updatePriorityCount(): Unit = {
    lowPriorityCount += 1
    if (lowPriorityCount > underlay.length / 2) {
      underlay.foreach(_.setHigh())
    }
  }

  def enqueue(workerCell: WorkerCell): WorkingQueue = {
    underlay.enqueue(workerCell)
    this
  }

  def reset(workerCell: WorkerCell): WorkingQueue = {
    @scala.annotation.tailrec
    def find(position: Int = 0): Int = {
      if(underlay(position) == workerCell) {
        position
      } else {
        find(position + 1)
      }
    }
    reset(workerCell, find())
  }

  def reset(workerCell: WorkerCell, idx: Int): WorkingQueue = {
    underlay.remove(idx)
    underlay.enqueue(workerCell)
    workerCell.setLow()
    updatePriorityCount()
    this
  }

}
object WorkingQueue {

  def empty = new WorkingQueue(mutable.Queue.empty[WorkerCell])

  def apply(total: Int)(workerFactory: Int => WorkerActor): WorkingQueue = {
    val builder = mutable.Queue.newBuilder[WorkerCell]
    var i = 0
    while(i < total) {
      val cell = WorkerCell(
        number = i,
        actor = workerFactory(i),
      )
      builder.addOne(cell)
      i += 1
    }
    new WorkingQueue(builder.result())
  }

}