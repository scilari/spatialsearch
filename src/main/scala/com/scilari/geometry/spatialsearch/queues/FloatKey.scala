package com.scilari.geometry.spatialsearch.queues

final class FloatKey[T](val key: Float, val value: T) extends Ordered[FloatKey[T]]{
  override def compare(that: FloatKey[T]): Int = {
    if (this.key > that.key) return -1
    if (this.key < that.key) return 1
    0
  }
}

object FloatKey{
  class ord[T] extends Ordering[FloatKey[T]] {
    override def compare(x: FloatKey[T], y: FloatKey[T]): Int = compare(x, y)
  }

  def compare(x: FloatKey[_], y: FloatKey[_]): Int = {
    if (x.key > y.key) return -1
    if (x.key < y.key) return 1
    0
  }
    //scala.math.Ordering.Float.compare(y.key, x.key)



}