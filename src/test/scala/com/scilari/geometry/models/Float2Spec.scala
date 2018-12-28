package com.scilari.geometry.models

import com.scilari.geometry.models.utils.Float2Utils
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

object Float2Spec{
  val point: Gen[Float2] = for{x <- arbitrary[Float]; y <- arbitrary[Float]} yield Float2(x,y)
  val niceFloat: Gen[Float] = Gen.chooseNum[Float](0.001f, 10.0f)
  val nicePoint: Gen[Float2] = for{x <- niceFloat; y <- niceFloat} yield Float2(x, y)
}

class Float2Spec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers{
  import Float2Spec._

  property("Basic operations"){
    forAll(point, point){ (p1, p2) =>
      p1 + p2 should be (Float2(p1.x + p2.x, p1.y + p2.y))
      p1 - p2 should be (Float2(p1.x - p2.x, p1.y - p2.y))
      p1 * p2 should be (Float2(p1.x * p2.x, p1.y * p2.y))
    }
  }

  property("Addition and point-wise multiplication should be commutative") {
    forAll(point, point) { (p1, p2) =>
      (p1 + p2) should be (p2 + p1)
      (p1 * p2) should be (p2 * p1)
    }
  }

  property("Vector space should obey triangle inequality") {
    forAll(point, point) { (p1, p2) =>
      p1.length + p2.length should be >= (p1 + p2).length
    }
  }

  property("Scalar multiplication should be distributive") {
    forAll(nicePoint, nicePoint, niceFloat) { (p1, p2, c) =>
      ((p1 + p2)*c - (p1*c + p2*c)).length/((p1 + p2)*c).length should be < 0.001f
    }
  }

  property("Dot product properties should hold"){
    forAll(nicePoint, nicePoint){ (p1, p2) =>
      p1.dot(p1) should be (p1.lengthSq)
      p1.dot(p2) should be (p2.dot(p1))
      p1.dot(p2) should be (p1.x*p2.x + p1.y*p2.y)
      p1.dot(p2) should be (p1.length * p2.length * Float2Utils.cosBetween(p1, p2) +- 0.01f)
      p1.perpDot(p2.rotated(-com.scilari.math.HalfPi)) should be (p1.dot(p2) +- 0.01f)
      p1.rotated(com.scilari.math.HalfPi).dot(p1) should be (0.0f +- 0.01f)
    }
  }

}
