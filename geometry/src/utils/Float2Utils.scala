package com.scilari.geometry.utils

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.math.FloatMath
import com.scilari.geometry.models.Position

object Float2Utils {
  def up: Float2 = Float2(0, 1)
  def down: Float2 = Float2(0, -1)
  def left: Float2 = Float2(-1, 0)
  def right: Float2 = Float2(1, 0)

  def max(p1: Float2, p2: Float2): Float2 =
    Float2(FloatMath.max(p1.x, p2.x), FloatMath.max(p1.y, p2.y))
  def min(p1: Float2, p2: Float2): Float2 =
    Float2(FloatMath.min(p1.x, p2.x), FloatMath.min(p1.y, p2.y))

  def toArray(f: Float2): Array[Double] = f.toDoubleArray
  def fromTuple(t: (Float, Float)): Float2 = Float2(t._1, t._2)

  def fromArray(a: Array[Float]): Float2 = Float2(a(0), a(1))
  def fromDoubleArray(a: Array[Double]): Float2 = Float2(a(0).toFloat, a(1).toFloat)

  def linSpace(start: Float2, end: Float2, n: Int): Array[Float2] = {
    val diff = end - start
    val ts = (0 until n).map(_.toFloat / (n - 1))
    ts.map { t => start + diff * t }.toArray
  }

  @inline
  def diffLengthSq(p1: Float2, p2: Float2): Float = {
    val dx = p1.x - p2.x
    val dy = p1.y - p2.y
    dx * dx + dy * dy
  }

  def sortByAngle[E <: Position](points: Array[E]): Array[E] = {
    def isUp(p: Float2): Boolean = p.y > 0f || (p.y == 0f && p.x > 0f)
    def angleComparator(p1: Position, p2: Position): Boolean = {
      val u1 = isUp(p1.position)
      val u2 = isUp(p2.position)
      (u1 && !u2) || (u1 == u2 && p1.position.perpDot(p2.position) > 0f)
    }

    points.sortWith(angleComparator)
  }

  @inline
  def sortByAngle[E <: Position](queryPoint: Float2, points: Array[E]): Array[E] = {
    val q = queryPoint
    def isUp(p: Float2): Boolean = p.y - q.y > 0f || (p.y - q.y == 0f && p.x - q.y > 0f)
    def angleComparator(p1: Position, p2: Position): Boolean = {
      val u1 = isUp(p1.position)
      val u2 = isUp(p2.position)
      (u1 && !u2) || (u1 == u2 && (p1.position - q.position).perpDot(p2.position - q.position) > 0f)
    }

    points.sortWith(angleComparator)

  }

  def fastAngleBetween(a: Float2, b: Float2): Float = {
    FloatMath.acos(fastCosBetween(a, b)).toFloat
  }

  def angleBetween(a: Float2, b: Float2): Float = {
    FloatMath.acos(cosBetween(a, b)).toFloat
  }

  def fastCosBetween(a: Float2, b: Float2): Float = {
    a.dot(b) * com.scilari.math.FastMath.invSqrt(a.lengthSq * b.lengthSq)
  }

  def cosBetween(a: Float2, b: Float2): Float = {
    a.dot(b) / FloatMath.sqrt(a.lengthSq * b.lengthSq)
  }

  import scala.language.implicitConversions

  implicit def Float2asDataPoint(p: Float2): DataPoint[Unit] = new DataPoint[Unit](p, ())

}
