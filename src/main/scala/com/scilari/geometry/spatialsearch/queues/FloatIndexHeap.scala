package com.scilari.geometry.spatialsearch.queues

import scala.annotation.tailrec
import scala.language.implicitConversions


class FloatIndexHeap(initialCapacity: Int = FloatHeap.defaultInitialCapacity) extends FloatPriorityQueue[Int] {

  val firstIndex = 1
  protected def left(k: Int): Int = k << 1
  protected def right(k: Int): Int = (k << 1) + 1
  protected def parent(k: Int): Int = k >> 1
  protected def leftAndRight(k: Int, arr: Array[Int]): Unit = {
    val l = k << 1
    arr(0) = l
    arr(1) = l + 1
  }

  protected[this] var capacity: Int = initialCapacity + 1
  private[this] var values = new Array[Int](capacity)
  private[this] var keys = new Array[Float](capacity)

  protected[this] var maxIndex = 0

  protected[this] def cmp(key1: Float, key2: Float): Boolean = key1 < key2

  keys(0) = if(cmp(0f, 1f)) Float.MinValue else Float.MaxValue // use as a sentinel in bubbleUp

  private[this] def move(to: Int, from: Int): Unit ={
    keys(to) = keys(from)
    values(to) = values(from)
  }

  private[this] def update(i: Int, key: Float, value: Int): Unit ={
    keys(i) = key
    values(i) = value
  }

  override def enqueue(key: Float, value: Int): Unit = {
    maxIndex += 1
    if(maxIndex == capacity) doubleCapacity()

    bubbleUp(key, value)
  }


  protected[this] def doubleCapacity(): Unit ={
    capacity *= 2
    val newValues = new Array[Int](capacity)
    val newKeys = new Array[Float](capacity)
    Array.copy(values, firstIndex, newValues, firstIndex, maxIndex - 1)
    Array.copy(keys, 0, newKeys, 0, maxIndex) // copy sentinel as well
    values = newValues
    keys = newKeys
  }

  private[this] def getValue(i: Int): Int = values(i)

  override def dequeue(): FloatKey[Int] = {
    val floatKey = new FloatKey(keys(firstIndex), getValue(firstIndex))
    popFirst()
    floatKey
  }

  override def dequeueValue(): Int = {
    val headValue: Int = getValue(firstIndex)
    popFirst()
    headValue
  }

  override def getValues: Seq[Int] = {
    val a = new Array[Int](maxIndex)
    Array.copy(values, firstIndex, a, 0, maxIndex)
    a
  }

  private[this] def popFirst(): Unit ={
    maxIndex -= 1
    bubbleDown(keys(maxIndex + 1), getValue(maxIndex + 1))
  }

  private[this] def bubbleUp(key: Float, value: Int): Unit ={

    @tailrec
    def makeRoomUp(pos: Int): Int = {
      val par = parent(pos)
      if(cmp(key, keys(par))){
        move(pos, par)
        makeRoomUp(par)
      } else {
        pos
      }
    }

    update(makeRoomUp(maxIndex), key, value)
  }

  protected[this] def bubbleDown(key: Float, value: Int): Unit ={
    val lr = new Array[Int](2)

    @tailrec
    def makeRoomDown(k: Int): Int = {
      leftAndRight(k, lr)
      val l = lr(0)
      val r = lr(1)

      if(l > maxIndex) {
        k
      } else {
        val child = if(l != maxIndex){
          if(cmp(keys(l), keys(r))) l else r
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


  override def head: FloatKey[Int] = new FloatKey(keys(firstIndex), getValue(firstIndex))

  override def headKey: Float = keys(firstIndex)

  def minKey: Float = headKey

  def maxKey: Float = ???

  override def isEmpty: Boolean = maxIndex == 0

  override def nonEmpty: Boolean = maxIndex != 0

  override def size: Int = maxIndex

  override def clear(): Unit = maxIndex = 0
}

object FloatIndexHeap{
  def apply(k: Float, e: Int, initialCapacity: Int = defaultInitialCapacity): FloatIndexHeap ={
    val h = new FloatIndexHeap(initialCapacity)
    h.enqueue(k, e)
    h
  }

  val defaultInitialCapacity = 7

}

