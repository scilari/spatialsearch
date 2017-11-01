package com.scilari

import scala.util.Random

/**
 * Created by iv on 2/22/14.
 */
package object math {
  val Pi: Float = Math.PI.toFloat
  val TwoPi: Float = 2f*Pi
  val HalfPi: Float= Pi/2f
  val PI: Double = Math.PI

  val rng = new Random()
  val random: Random = rng

  // trigonometric functions
  @inline def cos(x: Double): Double = Math.cos(x)
  @inline def sin(x: Double): Double = Math.sin(x)
  @inline def atan2(y: Double, x: Double): Double = Math.atan2(y, x)

  // float versions of trigonometric functions  TODO: find out if there is faster versions
  @inline def cos(x: Float): Float = Math.cos(x).toFloat
  @inline def sin(x: Float): Float = Math.sin(x).toFloat
  @inline def atan2(y: Float, x: Float): Float = Math.atan2(y, x).toFloat
  @inline def atan2(a: Array[Float]): Float = atan2(a(1), a(0))

  @inline def sqrt(x: Float): Float = Math.sqrt(x).toFloat
  @inline def abs(x: Float): Float = Math.abs(x)
  @inline def min(x: Float, y: Float): Float = Math.min(x, y)
  @inline def max(x: Float, y: Float): Float = Math.max(x, y)

  @inline def round(x: Float): Int = Math.round(x)
  @inline def floor(x: Float): Int = Math.floor(x).toInt
  @inline def ceil(x: Float): Int = Math.ceil(x).toInt

  @inline def signum(x: Float): Float = Math.signum(x)

  @inline def pow(x: Double, y: Double): Double = Math.pow(x, y)

  @inline def clamp(x: Float, floor: Float, ceil: Float): Float = min(max(x, floor), ceil)

  @inline def clampNormalize(x: Float, floor: Float, ceil: Float): Float = clamp(x, floor, ceil)/(ceil - floor)

  @inline def invSqrt(x: Float): Float = {
    val halfX = 0.5f*x
    val i = java.lang.Float.floatToIntBits(x)
    val j = 0x5f3759df - (i >> 1)
    val y = java.lang.Float.intBitsToFloat(j)
    y*(1.5f - halfX*y*y)
  }

}
