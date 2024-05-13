package com.scilari.geometry.models

import com.scilari.geometry.models.Circle
import com.scilari.math.FloatMath.sqrt
import javax.sound.sampled.Line

case class LineSegment(p1: Float2, p2: Float2) {
  def lengthSq: Float = Float2.distanceSq(p1, p2)
  def length: Float = sqrt(lengthSq)
  def dir: Float2 = p2 - p1
  def dirNormalized: Float2 = dir / length
  def normal: Float2 = dir.perp
  def center: Float2 = (p1 + p2) * 0.5f

  def endPoints = Seq(p1, p2)

  def intersection(that: LineSegment): Option[Float2] =
    LineSegment.intersectionLineSegment(this.p1, this.p2, that.p1, that.p2)

  def intersection(circle: Circle): List[Float2] = LineSegment.intersectionCircle(this, circle)

  def closestPoint(p: Float2): Float2 = {
    val ab = p1 - p2
    val proj = (p - p1).dot(ab)
    if (proj <= 0f) {
      p1
    } else {
      val fullAb = ab.lengthSq
      if (proj <= fullAb) {
        p2
      } else {
        p1 + ab * (proj / fullAb)
      }
    }
  }

  def distanceSq(p: Float2): Float = {
    val ab = p1 - p2
    val ap = p - p1

    val proj = ap.dot(ab)
    if (proj <= 0f) {
      ap.lengthSq
    } else {
      val fullAb = ab.dot(ab)
      if (proj >= fullAb) {
        val bp = p - p2
        bp.lengthSq
      } else {
        ap.lengthSq - (proj * proj) / fullAb
      }
    }
  }

  def moveEndpoints(dx1: Float = 0f, dx2: Float = 0f): LineSegment = {
    val vec = (p2 - p1).normalized
    LineSegment(p1 - vec * dx1, p2 + vec * dx2)
  }

  def scale(scale: Float): LineSegment = LineSegment(scale * this.p1, scale * this.p2)
  def scale(scale: Double): LineSegment = LineSegment(scale * this.p1, scale * this.p2)

  override def toString: String = s"LineSegment[$p1, $p2]"
}

object LineSegment {
  val epsilon = 0.0000001f

  def intersectionLineSegment(a1: Float2, b1: Float2, a2: Float2, b2: Float2): Option[Float2] = {
    val d = (a1.x - b1.x) * (a2.y - b2.y) - (a1.y - b1.y) * (a2.x - b2.x)
    if (math.abs(d) < epsilon) {
      None
    } else {
      val t = ((a1.x - a2.x) * (a2.y - b2.y) - (a1.y - a2.y) * (a2.x - b2.x)) / d
      val u = -((a1.x - b1.x) * (a1.y - a2.y) - (a1.y - b1.y) * (a1.x - a2.x)) / d
      if (t >= 0f && t <= 1 && u >= 0f && u <= 1f) {
        Option(
          Float2(a1.x + t * (b1.x - a1.x), a1.y + t * (b1.y - a1.y))
        )
      } else {
        None
      }
    }
  }

  def intersectionCircle(s: LineSegment, c: Circle): List[Float2] = {
    val p1 = s.p1
    val p2 = s.p2
    val cx = c.position.x
    val cy = c.position.y

    // Computationally useful constants
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val cdx = p1.x - cx
    val cdy = p1.y - cy

    // Solving (X - cx)^2 + (Y - cy)^2 - r^2 = 0 for t after plugging in X = p1.x + (p2.x - p1.x)*t, Y = ...
    val A = dx * dx + dy * dy
    val B = 2 * (dx * cdx + dy * cdy)
    val C = cdx * cdx + cdy * cdy - c.r * c.r

    val det = B * B - 4 * A * C
    if (A < epsilon || det < 0f) {
      Nil // no intersection
    } else {
      val ts = List(
        (-B + sqrt(det)) / (2 * A),
        (-B - sqrt(det)) / (2 * A)
      )

      ts.filter(t => t >= 0f && t <= 1f).map(t => Float2(p1.x + t * dx, p1.y + t * dy))
    }
  }

}
