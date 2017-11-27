package com.scilari.geometry.spatialsearch.queues

import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.implicitConversions

final class FloatHeap[E](initialCapacity: Int = 32) extends FloatPriorityQueue[E] {
  private[this] var values = new Array[Any](initialCapacity)
  private[this] var keys = new Array[Float](initialCapacity)
  private[this] implicit def anyToE(a: Any): E = a.asInstanceOf[E]

  private[this] val firstIndex = 1
  private[this] var maxIndex = 0
  private[this] var capacity = initialCapacity

  private[this] def left(k: Int) = 2*k
  private[this] def parent(k: Int) = k/2

  private[this] def move(to: Int, from: Int): Unit ={
    keys(to) = keys(from)
    values(to) = values(from)
  }

  private[this] def update(i: Int, key: Float, value: E): Unit ={
    keys(i) = key
    values(i) = value
  }

  override def enqueue(key: Float, value: E): Unit = {
    if(maxIndex == capacity - 1) doubleCapacity()
    maxIndex += 1
    bubbleUp(key, value)
  }

  private[this] def doubleCapacity(): Unit ={
    capacity *= 2
    val newValues = new Array[Any](capacity)
    val newKeys = new Array[Float](capacity)
    Array.copy(values, firstIndex, newValues, firstIndex, maxIndex)
    Array.copy(keys, firstIndex, newKeys, firstIndex, maxIndex)
    values = newValues
    keys = newKeys
  }

  override def dequeue(): FloatKey[E] = {
    val floatKey = new FloatKey[E](keys(firstIndex), values(firstIndex))
    popFirst()
    floatKey
  }

  override def dequeueValue(): E = {
    val headValue = values(firstIndex)
    popFirst()
    headValue
  }

  override def getAndClearAllValues(): Seq[E] = {
    val b = mutable.Buffer[E]()
    for(i <- 1 to maxIndex) b += values(i)
    clear()
    b
  }

  private[this] def popFirst(): Unit ={
    move(firstIndex, maxIndex)
    maxIndex -= 1
    bubbleDown()
  }

  private[this] def bubbleUp(key: Float, value: E): Unit ={

    @tailrec
    def makeRoom(pos: Int): Int = {
      val par = parent(pos)
      if(pos > 1 && key < keys(par)){
        move(pos, par)
        makeRoom(par)
      } else
        pos
    }

    update(makeRoom(maxIndex), key, value)

  }

  private[this] def bubbleDown(): Unit ={
    val headKey = keys(firstIndex)
    val headValue = values(firstIndex)

    @tailrec
    def makeRoom(k: Int): Int = {
      val L = left(k)
      if(L > maxIndex)
        k
      else{
        val R = L + 1
        val child = if(L != maxIndex && keys(L) > keys(R)) R else L
        if (headKey > keys(child)){
          move(k, child)
          makeRoom(child)
        } else
          k
      }
    }

    update(makeRoom(firstIndex), headKey, headValue)
  }


  override def head: FloatKey[E] = new FloatKey(keys(firstIndex), values(firstIndex))

  override def headKey: Float = keys(firstIndex)

  override def isEmpty: Boolean = maxIndex == 0

  override def nonEmpty: Boolean = maxIndex != 0

  override def size: Int = maxIndex

  override def clear(): Unit = maxIndex = 0
}
