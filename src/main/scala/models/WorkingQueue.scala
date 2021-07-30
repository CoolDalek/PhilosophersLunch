package models

import actors.Worker.WorkerActor

import scala.collection.mutable

class WorkingQueue private(val underlay: mutable.Queue[WorkerCell]) {

  private var lowPriorityCount = 0

  def apply(idx: Int): WorkerCell = underlay(idx)

  def length: Int = underlay.length

  private def updatePriorityCount(): Unit = {
    lowPriorityCount += 1
    if (lowPriorityCount == underlay.length) {
      var i = 0
      val half = math.max(1, underlay.length / 2)
      while (i < half) {
        underlay(i).setHigh()
        i += 1
      }
      lowPriorityCount = underlay.length - half
    }
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