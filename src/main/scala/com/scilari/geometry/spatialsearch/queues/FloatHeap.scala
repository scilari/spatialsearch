package com.scilari.geometry.spatialsearch.queues

// TODO: fix to use classTag
final class FloatHeap[E](initialCapacity: Int = 32) extends FloatPriorityQueue[E] {
  private[this] var values = new Array[AnyRef](initialCapacity)
  private[this] var keys = new Array[Float](initialCapacity)
  private[this] var maxIndex = 0
  private[this] var capacity = initialCapacity

  private[this] def left(k: Int) = 2*k
  private[this] def parent(k: Int) = k/2

  private[this] def move(i: Int, j: Int): Unit ={
    keys(i) = keys(j)
    values(i) = values(j)
  }

  private[this] def update(i: Int, key: Float, value: AnyRef): Unit ={
    keys(i) = key
    values(i) = value
  }


  override def enqueue(key: Float, value: E): Unit = {
    if(maxIndex == capacity - 1) doubleCapacity()
    maxIndex += 1
    bubbleUp(key, value)
  }

  def doubleCapacity(): Unit ={
    capacity *= 2
    val newValues = new Array[AnyRef](capacity)
    val newKeys = new Array[Float](capacity)
    Array.copy(values, 1, newValues, 1, maxIndex)
    Array.copy(keys, 1, newKeys, 1, maxIndex)
    values = newValues
    keys = newKeys
  }

  override def dequeue(): FloatKey[E] = {
    val floatKey = new FloatKey[E](keys(1), values(1).asInstanceOf[E])
    popFirst()
    floatKey
  }

  override def dequeueValue(): E = {
    val headValue = values(1)
    popFirst()
    headValue.asInstanceOf[E]
  }

  private def popFirst(): Unit ={
    move(1, maxIndex)
    maxIndex -= 1
    bubbleDown()
  }

  private def bubbleUp(key: Float, value: E): Unit ={
    var pos = maxIndex
    var par = parent(pos)
    while(pos > 1 && key < keys(par)){
      move(pos, par)
      pos = parent(pos)
      par = parent(pos)
    }

    update(pos, key, value.asInstanceOf[AnyRef])
  }

  private def bubbleDown(): Unit ={
    val headKey = keys(1)
    val headValue = values(1)
    var k = 1

    @inline def findUpdateIndex(): Int = {
      var L = left(k)
      var R = L+1
      while (L <= maxIndex) {
        val child = if(L != maxIndex && keys(L) > keys(R)) R else L
        if (headKey > keys(child))
          move(k, child)
        else
          return k

        k = child
        L = left(k)
        R = L+1
      }
      k
    }

    update(findUpdateIndex(), headKey, headValue)

  }


  override def head: FloatKey[E] = new FloatKey(keys(1), values(1).asInstanceOf[E])

  override def headKey: Float = keys(1)

  override def isEmpty: Boolean = maxIndex == 0

  override def nonEmpty: Boolean = maxIndex != 0

  override def size: Int = maxIndex

  override def clear(): Unit = maxIndex = 0
}

object FloatHeap{
  def main(args: Array[String]): Unit ={
    val heap = new FloatHeap[String](2)
    heap.enqueue(0.4f, "D")
    heap.enqueue(0.5f, "E")
    heap.enqueue(0.2f, "B")
    heap.enqueue(0.1f, "A")
    heap.enqueue(0.3f, "C")
    heap.enqueue(-0.4f, "d")
    heap.enqueue(-0.5f, "e")
    heap.enqueue(-0.2f, "b")
    heap.enqueue(-0.1f, "a")
    heap.enqueue(-0.3f, "c")

    println("size: " + heap.size)
    while(heap.nonEmpty){
      val s = heap.dequeueValue()
      println(s)
    }

  }
}
