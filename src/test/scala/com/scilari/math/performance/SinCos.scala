package com.scilari.math.performance

import com.scilari.geometry.spatialsearch.TestUtils.Timing._
import com.scilari.math.{FastMath, random}
import org.scalatest.{FlatSpec, Matchers}

class SinCos extends FlatSpec with Matchers {
  val runCount = 10000
  val warmUpCount = 100
  val valCount = 1000
  val rs: Seq[Float] = Array.fill(valCount)(4*com.scilari.math.Pi*(random.nextFloat() - 0.5f))

  "Sine" should "be faster than the standard library version" in {
    val t = warmUpAndMeasureTime({
      var i = 0
      while(i < valCount){
        BlackHole.consumeAny(FastMath.sin(rs(i)))
        i += 1
      }
    }, runCount, warmUpCount)

    val tLib = warmUpAndMeasureTime({
      var i = 0
      while(i < valCount){
        BlackHole.consumeAny(math.sin(rs(i)))
        i += 1
      }
    }, runCount, warmUpCount)

    t should be < tLib
    info(s"Ratio of FastMath sin to lib sin: ${t/tLib}")
    info(s"Time taken per operation: ${t/(runCount*valCount)}")
  }

  "Cosine" should "be faster than the standard library version" in {
    val t = warmUpAndMeasureTime({
      var i = 0
      while(i < valCount){
        BlackHole.consumeAny(FastMath.cos(rs(i)))
        i += 1
      }
    }, runCount, warmUpCount)

    val tLib = warmUpAndMeasureTime({
      var i = 0
      while(i < valCount){
        BlackHole.consumeAny(math.cos(rs(i)))
        i += 1
      }
    }, runCount, warmUpCount)

    t should be < tLib
    info(s"Ratio of FastMath cos to lib cos: ${t/tLib}")
    info(s"Time taken per operation: ${t/(runCount*valCount)}")
  }


}
