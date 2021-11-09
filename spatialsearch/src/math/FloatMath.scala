package com.scilari.math

import scala.util.Random

/**
 * Convenience methods for Float math. Mostly redirected to corresponding java.Math methods.
 */
object FloatMath {
  val Pi: Float = Math.PI.toFloat
  val TwoPi: Float = 2f * Pi
  val HalfPi: Float = Pi / 2f

  val rng = new Random()
  val random: Random = rng
  
  @inline def cos(x: Float): Float = Math.cos(x.toDouble).toFloat

  @inline def sin(x: Float): Float = Math.sin(x.toDouble).toFloat

  @inline def atan2(y: Float, x: Float): Float = Math.atan2(y.toDouble, x.toDouble).toFloat

  @inline def atan2(a: Array[Float]): Float = atan2(a(1), a(0))

  @inline def acos(x: Float): Float = Math.acos(x.toDouble).toFloat

  @inline def sqrt(x: Float): Float = Math.sqrt(x.toDouble).toFloat

  @inline def abs(x: Float): Float = Math.abs(x)

  @inline def min(x: Float, y: Float): Float = Math.min(x, y)

  @inline def max(x: Float, y: Float): Float = Math.max(x, y)

  @inline def round(x: Float): Int = Math.round(x)

  @inline def floor(x: Float): Int = Math.floor(x.toDouble).toInt

  @inline def ceil(x: Float): Int = Math.ceil(x.toDouble).toInt

  @inline def signum(x: Float): Float = Math.signum(x)

  @inline def pow(x: Double, y: Double): Double = Math.pow(x, y)

  @inline def clamp(x: Float, floor: Float, ceil: Float): Float = min(max(x, floor), ceil)

  @inline def clampNormalize(x: Float, floor: Float, ceil: Float): Float = (clamp(x, floor, ceil) - floor) / (ceil - floor)

  @inline def mean(xs: Seq[Float]): Float = xs.sum / xs.length

  @inline def variance(xs: Seq[Float]): Float = {
    val m = mean(xs)
    xs.map { x => (x - m) * (x - m) }.sum / xs.size
  }

  @inline def deviation(xs: Seq[Float]): Float = sqrt(variance(xs))

}

