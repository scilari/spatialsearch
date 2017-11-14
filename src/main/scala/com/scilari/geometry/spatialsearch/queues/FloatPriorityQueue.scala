package com.scilari.geometry.spatialsearch.queues

import scala.collection.mutable

trait FloatPriorityQueue[E] {
  def enqueue(key: Float, e: E): Unit = enqueue(new FloatKey(key, e))
  def enqueue(e: FloatKey[E]): Unit = enqueue(e.key, e.value)

  def dequeue(): FloatKey[E]
  def dequeueValue(): E = dequeue().value

  def getAndClearAllValues(): Seq[E] = {
    val b = mutable.Buffer[E]()
    while(nonEmpty)b += dequeueValue()
    b
  }

  def apply(elems: FloatKey[E]*): FloatPriorityQueue[E] = {
    elems.foreach(e => enqueue(e))
    this
  }

  def apply(key: Float, e: E): this.type = {
    enqueue(key, e)
    this
  }

  def head: FloatKey[E]
  def headKey: Float = head.key
  def headValue: E = head.value
  def isEmpty: Boolean
  def nonEmpty: Boolean = !isEmpty
  def size: Int

  def clear(): Unit
}
