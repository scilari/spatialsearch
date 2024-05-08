package com.scilari.geometry.models

case class Interval(min: Float, max: Float, minIx: Int = -1, maxIx:Int = -1) {
  def intersects(other: Interval): Boolean = max >= other.min && other.max >= min
  def overlap(other: Interval): Float = math.min(max - other.min, other.max - min)
  def contains(x: Float): Boolean = x >= min && x <= max
  def contactIndex(other: Interval): Int = if(max - other.min > other.max - min) minIx else maxIx

  override def toString: String = s"Interval [$min, $max]"
}

object Interval {
  def apply(xs: Array[Float]): Interval = {
    var minIx = 0
    var maxIx = 0
    var min = xs(0)
    var max = xs(0)
    val n = xs.length
    var i = 1
    while(i < n) {
      if(xs(i) < min){
        min = xs(i)
        minIx = i
      }
      if(xs(i) > max){
        max = xs(i)
        maxIx = i
      }

      i += 1
    }
    Interval(min, max, minIx, maxIx)
  }

  def overlap(as: Array[Float], bs: Array[Float]): Float = {
    val na = as.length
    var minA = as(0)
    var maxA = as(0)
    val nb = bs.length
    var minB = bs(0)
    var maxB = bs(0)

    var i = 1
    while(i < na) {
      val a = as(i)
      minA = math.min(minA, a)
      maxA = math.max(maxA, a)
      i += 1
    }
    var j = 1
    while(j < nb) {
      val b = bs(j)
      minB = math.min(minB, b)
      maxB = math.max(maxB, b)
      j += 1
    }

    math.min(maxA - minB, maxB - minA)
  }
}
