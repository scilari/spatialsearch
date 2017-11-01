package com.scilari.geometry.spatialsearch.queues

class JavaPriorityQueueWrapper[T] extends FloatPriorityQueue[T] {
  val queue = new java.util.PriorityQueue[FloatKey[T]](50, Ordering.by[FloatKey[T], Float](_.key))

  override def enqueue(e: FloatKey[T]): Unit = queue.offer(e)

  override def enqueue(key: Float, e: T): Unit = new FloatKey[T](key, e)

  override def dequeue(): FloatKey[T] = queue.poll()

  override def head: FloatKey[T] = queue.peek()

  override def isEmpty: Boolean = queue.isEmpty

  override def size: Int = queue.size()

  override def clear(): Unit = queue.clear()
}
