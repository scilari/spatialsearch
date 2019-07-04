package com.scilari.geometry.spatialsearch.queues



class ArrayHeap[E](initialCapacity: Int = ArrayHeap.defaultInitialCapacity) extends FloatPriorityQueue[E]{
  private[this] var values = new Array[AnyRef](initialCapacity)
  private[this] var keys = new Array[Float](initialCapacity)
  private[this] var s = 0
  private[this] var capacity = initialCapacity

  override def enqueue(e: FloatKey[E]): Unit = {
    enqueue(e.key, e.value)
  }

  override def enqueue(key: Float, value: E): Unit ={
    if(isFull){
      doubleCapacity()
      enqueue(key, value)
    } else {
      var i = s
      while(i > 0 && keys(i - 1) < key) i -= 1
      s += 1
      val j = i + 1
      Array.copy(keys, i, keys, j, s - j)
      keys(i) = key
      Array.copy(values, i, values, j, s - j)
      values(i) = value.asInstanceOf[AnyRef]
    }
  }

  def dequeue(): FloatKey[E] ={
    s -= 1
    new FloatKey(keys(s), values(s).asInstanceOf[E])
  }

  override def dequeueValue(): E = {
    s -= 1
    values(s).asInstanceOf[E]

  }

  def doubleCapacity(): Unit ={
    capacity *= 2
    val newData = new Array[AnyRef](capacity)
    val newKeys = new Array[Float](capacity)
    Array.copy(values, 0, newData, 0, s)
    Array.copy(keys, 0, newKeys, 0, s)
    values = newData
    keys = newKeys
  }

  def isEmpty: Boolean = s == 0

  def head: FloatKey[E] = new FloatKey(keys(s-1), values(s-1).asInstanceOf[E])

  override def headKey: Float = keys(s-1)

  override def clear(): Unit = s = 0

  def headElement: E = values(s-1).asInstanceOf[E]

  def size: Int = s

  def isFull: Boolean = s >= capacity

}

object ArrayHeap{
  def apply[E](k: Float, e: E, initialCapacity: Int = defaultInitialCapacity): ArrayHeap[E] ={
    val h = new ArrayHeap[E](initialCapacity)
    h.enqueue(k, e)
    h
  }


  val defaultInitialCapacity: Int = 32
}
