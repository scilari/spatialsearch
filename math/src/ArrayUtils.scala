package com.scilari.math

import com.scilari.math.FloatMath._

object ArrayUtils {
  def sum(a: Array[Float]): Float = a.sum

  def cumsum(a: Array[Double]): Array[Double] = {
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

  def multiply(a: Array[Float], c: Float): Array[Float] = a.map{_ * c}

  def add(a: Array[Float], b: Array[Float]): Array[Float] = a.lazyZip(b).map(_ + _).toArray

  def mean(a: Array[Float]): Float = a.sum/a.length

  def weightedMean(a: Array[Float], w: Array[Float]): Float = a.lazyZip(w).map(_*_).sum/w.sum

  def meanAngle(a: Array[Float]): Float = {
    val sumX = a.map{cos}.sum
    val sumY = a.map{sin}.sum
    if(sumX == 0 && sumY == 0) a(0) else atan2(sumY, sumX) // return the first angle, if the direction vectors add to zero
  }

  def weightedMeanAngle(a: Array[Float], w: Array[Float]): Float = {
    val ys = a.map{sin}
    val xs = a.map{cos}
    val wys = ys.lazyZip(w).map(_*_)
    val wxs = xs.lazyZip(w).map(_*_)
    val sumX = wxs.sum
    val sumY = wys.sum
    if(sumX == 0 && sumY == 0) a(0) else atan2(sumY, sumX)
  }

  def rotate(a: Array[Float], angle: Float): Array[Float] = {
    val s = FloatMath.sin(angle).toFloat; val c = FloatMath.cos(angle).toFloat
    val rx = a(0)*c - a(1)*s
    val ry = a(0)*s + a(1)*c
    a(0) = rx; a(1) = ry
    a
  }

  def linSpace(a: Float, b: Float, n: Int): Array[Float] = {
    Array.tabulate(n)(i => a + i*(b - a)/(n-1))
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
