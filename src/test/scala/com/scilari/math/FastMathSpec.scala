package com.scilari.math

import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

object FastMathSpec{
  val tolerance = 0.025f
  val niceFloat: Gen[Float] = Gen.choose[Float](-TwoPi, TwoPi)
  val zeroPoints: Gen[Float] = Gen.choose[Int](-2, 2).map{_ * Pi}
  val onePoints: Gen[Float] = Gen.choose[Int](-2, 2).map{_ * TwoPi + HalfPi}
  val minusPoints: Gen[Float] = Gen.choose[Int](-2, 2).map{_ * TwoPi - HalfPi}
}

class FastMathSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers{
  import FastMathSpec._

  property("Exact sin values at certain points"){
    forAll(zeroPoints){ x => FastMath.sin(x) shouldBe 0f }
    forAll(onePoints){ x => FastMath.sin(x) shouldBe 1f }
    forAll(minusPoints){ x => FastMath.sin(x) shouldBe -1f }
  }

  property("Sin accuracy"){
    forAll(niceFloat){ x =>
      FastMath.sin(x) shouldBe (sin(x) +- tolerance)
    }
  }

  property("Cos accuracy"){
    forAll(niceFloat){ x =>
      FastMath.cos(x) shouldBe (cos(x) +- tolerance)
    }
  }


}
