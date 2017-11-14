package com.scilari.geometry.spatialsearch.queues


import scala.collection.mutable

class ScalaPriorityQueueWrapper[E] extends FloatPriorityQueue[E] {
  val queue = new mutable.PriorityQueue[FloatKey[E]]()

  override def enqueue(e: FloatKey[E]): Unit = queue.enqueue(e)

  override def dequeue(): FloatKey[E] = queue.dequeue()

  override def head: FloatKey[E] = queue.head

  override def isEmpty: Boolean = queue.isEmpty

  override def size: Int = queue.size

  override def clear(): Unit = queue.clear()

}

