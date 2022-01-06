package com.scilari.geometry.spatialsearch.queues

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

class FloatMinHeap[E](initialCapacity: Int = FloatMinHeap.defaultInitialCapacity) {

  val firstIndex = 1
  private[this] def left(k: Int): Int = k << 1
  private[this] def right(k: Int): Int = (k << 1) + 1
  private[this] def parent(k: Int): Int = k >> 1

  protected var capacity: Int = initialCapacity + 1
  protected var values = new Array[Any](capacity)
  protected var keys = new Array[Float](capacity)

  private[this] var maxIndex = 0
  protected def cmp(key1: Float, key2: Float): Boolean = key1 < key2

  keys(0) = if (cmp(0f, 1f)) Float.MinValue else Float.MaxValue // use as a sentinel in bubbleUp

  private[this] def move(to: Int, from: Int): Unit = {
    keys(to) = keys(from)
    values(to) = values(from)
  }

  private[this] def update(i: Int, key: Float, value: E): Unit = {
    keys(i) = key
    values(i) = value
  }

  def enqueue(key: Float, value: E): Unit = {
    maxIndex += 1
    if (maxIndex == capacity) doubleCapacity()
    bubbleUp(key, value)
  }

  protected[this] def doubleCapacity(): Unit = {
    capacity *= 2
    val newValues = new Array[Any](capacity)
    val newKeys = new Array[Float](capacity)
    Array.copy(values, firstIndex, newValues, firstIndex, maxIndex - 1)
    Array.copy(keys, 0, newKeys, 0, maxIndex) // copy sentinel as well
    values = newValues
    keys = newKeys
  }

  private[this] def getValue(i: Int): E = values(i).asInstanceOf[E]

  def dequeue(): FloatKey[E] = {
    val floatKey = FloatKey[E](keys(firstIndex), getValue(firstIndex))
    popFirst()
    floatKey
  }

  def dequeueValue(): E = {
    val headValue: E = getValue(firstIndex)
    popFirst()
    headValue
  }

  def peekValuesToBuffer(b: ArrayBuffer[E]): ArrayBuffer[E] = {
    var i = firstIndex
    while (i <= maxIndex) {
      b += values(i).asInstanceOf[E]
      i += 1
    }
    b
  }

  def peekValues: ArrayBuffer[E] = peekValuesToBuffer(new ArrayBuffer[E](maxIndex))

  def getKeysAndValues: ArrayBuffer[FloatKey[E]] = {
    val b = ArrayBuffer[Any]()
    var i = firstIndex
    while (i < maxIndex) { // TODO: check if this is ok
      b += FloatKey(keys(i), values(i))
      i += 1
    }
    b.asInstanceOf[ArrayBuffer[FloatKey[E]]]
  }

  private[this] def popFirst(): Unit = {
    maxIndex -= 1
    bubbleDown(keys(maxIndex + 1), getValue(maxIndex + 1))
  }

  private[this] def bubbleUp(key: Float, value: E): Unit = {

    @tailrec
    def makeRoomUp(pos: Int): Int = {
      val par = parent(pos)
      if (cmp(key, keys(par))) {
        move(pos, par)
        makeRoomUp(par)
      } else {
        pos
      }
    }

    update(makeRoomUp(maxIndex), key, value)
  }

  protected def bubbleDown(key: Float, value: E): Unit = {
    @tailrec
    def makeRoomDown(k: Int): Int = {
      val l = left(k)
      val r = l + 1

      if (l > maxIndex) {
        k
      } else {
        val child = if (l != maxIndex) {
          if (cmp(keys(l), keys(r))) l else r
        } else {
          l
        }

        if (cmp(keys(child), key)) {
          move(k, child)
          makeRoomDown(child)
        } else {
          k
        }
      }
    }

    update(makeRoomDown(firstIndex), key, value)
  }

  def peek: E = getValue(firstIndex)

  def head: FloatKey[E] = FloatKey(keys(firstIndex), getValue(firstIndex))

  protected def headKey = keys(firstIndex)

  def minKey: Float = headKey

  def maxKey: Float = keys.max

  def isEmpty: Boolean = maxIndex == 0

  def nonEmpty: Boolean = maxIndex != 0

  def size: Int = maxIndex

  def clear(): Unit = maxIndex = 0
}

object FloatMinHeap {
  def apply[E](k: Float, e: E, initialCapacity: Int = defaultInitialCapacity): FloatMinHeap[E] = {
    val h = new FloatMinHeap[E](initialCapacity)
    h.enqueue(k, e)
    h
  }

  val defaultInitialCapacity = 7

}
