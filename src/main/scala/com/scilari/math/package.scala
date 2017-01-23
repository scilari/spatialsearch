package com.scilari

import scala.util.Random

/**
 * Created by iv on 2/22/14.
 */
package object math {
  val Pi = Math.PI.toFloat
  val TwoPi = 2f*Pi
  val HalfPi = Pi/2f
  val PI = Math.PI

  val rng = new Random()
  val random = rng

  // trigonometric functions
  @inline def cos(x: Double) = Math.cos(x)
  @inline def sin(x: Double) = Math.sin(x)
  @inline def atan2(y: Double, x: Double) = Math.atan2(y, x)

  // float versions of trigonometric functions  TODO: find out if there is faster versions
  @inline def cos(x: Float) = Math.cos(x).toFloat
  @inline def sin(x: Float) = Math.sin(x).toFloat
  @inline def atan2(y: Float, x: Float): Float = Math.atan2(y, x).toFloat
  @inline def atan2(a: Array[Float]): Float = atan2(a(1), a(0))

  @inline def sqrt(x: Float) = Math.sqrt(x).toFloat
  @inline def abs(x: Float) = Math.abs(x)
  @inline def min(x: Float, y: Float) = Math.min(x, y)
  @inline def max(x: Float, y: Float) = Math.max(x, y)

  @inline def round(x: Float) = Math.round(x)
  @inline def floor(x: Float) = Math.floor(x).toInt
  @inline def ceil(x: Float) = Math.ceil(x).toInt

  @inline def signum(x: Float) = Math.signum(x)

  @inline def pow(x: Double, y: Double) = Math.pow(x, y)

  @inline def clamp(x: Float, floor: Float, ceil: Float): Float = if( x < floor ) floor else if( x > ceil ) ceil else x

  @inline def clampNormalize(x: Float, floor: Float, ceil: Float): Float = clamp(x, floor, ceil)/(ceil - floor)

  @inline def invSqrt(x: Float): Float = {
    val xhalf = 0.5f*x
    val i = java.lang.Float.floatToIntBits(x)
    val j = 0x5f3759df - (i >> 1)
    val y = java.lang.Float.intBitsToFloat(j)
    y*(1.5f - xhalf*y*y)
  }

  def sum(a: Array[Float]): Float = a.sum

  def cumsum(a: Array[Double]) = {
    val sumArray = new Array[Double](a.length)
    sumArray(0) = a(0)
    for(i <- 1 until a.length) sumArray(i) = sumArray(i-1) + a(i)
    sumArray
  }

  def normalizeSum(a: Array[Float]): Array[Float] = {
    val invSum = 1.0f/a.sum
    for(i <- a.indices) a(i) *= invSum
    a
  }

  @inline def normalizeSumInPlace(a: Array[Float]): Array[Float] = {
    val n = a.length
    var i = 0
    var sum = 0f
    while(i < n){
      sum += a(i)
      i += 1
    }
    val invSum = 1f/sum
    i = 0
    while(i < n){
      a(i) = a(i)*invSum
      i += 1
    }
    a
  }

  def normalizeSum(a: Array[Float], to: Float): Array[Float] = multiply(normalizeSum(a), to)

  def multiply(a: Array[Float], c: Float) = a.map{_ * c}

  def add(a: Array[Float], b: Array[Float]) = (a, b).zipped.map(_+_)

  def mean(a: Array[Float]) = a.sum/a.length



  def weightedMean(a: Array[Float], w: Array[Float]): Float = (a, w).zipped.map(_*_).sum/w.sum

  def meanAngle(a: Array[Float]) = {
    val sumX = a.map{math.cos(_)}.sum
    val sumY = a.map{math.sin(_)}.sum
    if(sumX == 0 && sumY == 0) a(0) else atan2(sumY, sumX) // return the first angle, if the direction vectors add to zero
  }

  def weightedMeanAngle(a: Array[Float], w: Array[Float]) = {
    val ys = a.map{math.sin(_)}
    val xs = a.map{math.cos(_)}
    val wys = (ys, w).zipped.map(_*_)
    val wxs = (xs, w).zipped.map(_*_)
    math.atan2(wys.sum, wxs.sum) // TODO: return the first angle if direction vectors add to zero
  }

  def rotate(a: Array[Float], angle: Float) = {
    val s = Math.sin(angle).toFloat; val c = Math.cos(angle).toFloat
    val rx = a(0)*c - a(1)*s
    val ry = a(0)*s + a(1)*c
    a(0) = rx; a(1) = ry
    a
  }

  def binaryFindIndex(a: Array[Double], key: Double): Int = {
      val ix = java.util.Arrays.binarySearch(a, key)
      if (ix >= 0) ix else -ix - 1
  }

  def binaryFindIndex(a: Array[Int], key: Int): Int = {
    val ix = java.util.Arrays.binarySearch(a, key)
    if (ix >= 0) ix else -ix - 1
  }

  def binaryFindIndex(a: Array[Double], fromIndex: Int, toIndex: Int, key: Double): Int = {
    val ix = java.util.Arrays.binarySearch(a, fromIndex, toIndex, key)
    if (ix >= 0) ix else -ix - 1
  }

  def binaryFind(a: Array[Double], key: Double): Double = {
    a(binaryFindIndex(a, key))
  }

  def binaryFind(a: Array[Double], fromIndex: Int, toIndex: Int, key: Double): Double = {
    a(binaryFindIndex(a, fromIndex, toIndex, key))
  }


}
