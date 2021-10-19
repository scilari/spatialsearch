package com.scilari.geometry.spatialsearch.queues

case class FloatKey[T](key: Float, value: T) extends Ordered[FloatKey[T]]{
  override def compare(that: FloatKey[T]): Int = {
    if (this.key > that.key) -1 else if (this.key < that.key) 1 else 0
  }
}

object FloatKey{
  class Ord[T] extends Ordering[FloatKey[T]] {
    override def compare(x: FloatKey[T], y: FloatKey[T]): Int = compare(x, y)
  }

  def compare(x: FloatKey[_], y: FloatKey[_]): Int = {
    if (x.key > y.key)  -1 else if (x.key < y.key) 1 else 0
  }
}
