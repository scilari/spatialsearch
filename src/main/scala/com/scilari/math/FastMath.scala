package com.scilari.math
import com.scilari.math.FloatMath._

/**
  * Fast approximates for selected mathematical functions
  */
object FastMath {

  private object SinConstants {
    // computed constants for the polynomial
    val qN = 13
    val qA = 12
    val qP = 14
    val qR: Int  = 2*qN - qP
    val qS: Int = qN + qP + 1 - qA

    val Q12 = 4096
    val InvQ12: Float = 1f/Q12 // 1/2^12
    val InvTwoPi: Float = 1f/TwoPi
    val Q15 = 32768 // 2^15
    def radToFixed(x: Float): Int = (x*Q15*InvTwoPi).toInt
  }

  /**
    * 3 rd order polynomial fixed point approximation from http://www.coranac.com/2009/07/sines/
    */
  @inline private def sinFixed15(xx: Int): Int = {
    import SinConstants._
    var x = xx << (30 - qN)
    x = if((x ^ (x << 1)) < 0) (1 << 31) - x else x
    x = x >> (30 - qN)
    x * ((3 << qP) - (x*x >> qR)) >> qS
  }

  //
  /**
    * 3rd order polynomial approximation that maps to fixed point and back: http://www.coranac.com/2009/07/sines/
    * @return Approximate of sin(x)
    */
  @inline def sin(x: Float): Float = {
    import SinConstants._
    sinFixed15(radToFixed(x))*InvQ12
  }

  /**
    * 3rd order polynomial approximation that maps to fixed point and back: http://www.coranac.com/2009/07/sines/
    * @return Approximate of cos(x), computed as sin(HalfPi - x)
    */
  @inline def cos(x: Float): Float = sin(HalfPi - x)

  /**
    * Inverse square root approximation
    * @return Approximate of 1.0/sqrt(x)
    */
  @inline def invSqrt(x: Float): Float = {
    val halfX = 0.5f*x
    val i = java.lang.Float.floatToIntBits(x)
    val j = 0x5f3759df - (i >> 1)
    val y = java.lang.Float.intBitsToFloat(j)
    y*(1.5f - halfX*y*y)
  }

}
