package com.scilari.geometry.utils

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.math.FloatMath

object Float2Utils {
  def up: Float2 = Float2(0, 1)
  def down: Float2 = Float2(0, -1)
  def left: Float2 = Float2(-1, 0)
  def right: Float2 = Float2(1, 0)

  def max(p1: Float2, p2: Float2): Float2 = Float2(FloatMath.max(p1.x, p2.x), FloatMath.max(p1.y, p2.y))
  def min(p1: Float2, p2: Float2): Float2 = Float2(FloatMath.min(p1.x, p2.x), FloatMath.min(p1.y, p2.y))

  def toArray(f: Float2): Array[Double] = f.toDoubleArray
  def fromTuple(t: (Float, Float)): Float2 = Float2(t._1, t._2)

  def fromArray(a: Array[Float]): Float2 = Float2(a(0), a(1))
  def fromDoubleArray(a: Array[Double]): Float2 = Float2(a(0).toFloat, a(1).toFloat)

  def linSpace(start: Float2, end: Float2, n: Int): Array[Float2] = {
    val diff = end - start
    val ts = (0 until n).map(_.toFloat/(n-1))
    ts.map{ t => start + diff*t}.toArray
  }

  @inline
  def diffLengthSq(p1: Float2, p2: Float2): Float = {
    val dx = p1.x - p2.x
    val dy = p1.y - p2.y
    dx*dx + dy*dy
  }

  def sortByAngle(points: Array[Float2]): Array[Float2] = {
    def isUp(p: Float2): Boolean = p.y > 0f || (p.y == 0f && p.x > 0f)
    def angleComparator (p1: Float2, p2: Float2): Boolean = {
      val u1 = isUp(p1)
      val u2 = isUp(p2)
      (u1 && !u2) || (u1 == u2 &&  p1.perpDot(p2) > 0f)
    }

    points.sortWith(angleComparator)
  }

  @inline
  def sortByAngle[E <: Float2](queryPoint: Float2, points: Seq[E]): Seq[E] = {
    val q = queryPoint
    def isUp(p: Float2): Boolean = p.y - q.y > 0f || (p.y - q.y == 0f && p.x - q.y > 0f)
    def angleComparator (p1: Float2, p2: Float2): Boolean = {
      val u1 = isUp(p1)
      val u2 = isUp(p2)
      (u1 && !u2) || (u1 == u2 &&  (p1-q).perpDot(p2-q) > 0f)
    }

    points.sortWith(angleComparator)

  }

  def fastAngleBetween(a: Float2, b: Float2): Float = {
    FloatMath.acos(fastCosBetween(a, b)).toFloat
  }

  def angleBetween(a: Float2, b: Float2): Float = {
    FloatMath.acos(cosBetween(a, b)).toFloat
  }

  def fastCosBetween(a: Float2, b: Float2): Float ={
    a.dot(b)*com.scilari.math.FastMath.invSqrt(a.lengthSq*b.lengthSq)
  }

  def cosBetween(a: Float2, b: Float2): Float ={
    a.dot(b)/FloatMath.sqrt(a.lengthSq*b.lengthSq)
  }

  import scala.language.implicitConversions

  implicit def Float2asDataPoint(p: Float2): DataPoint[Unit] = new DataPoint[Unit](p, ())

}
