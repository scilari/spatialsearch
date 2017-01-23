package com.scilari.geometry.models
import com.scilari.math._


/**
 * Created by iv on 10.2.2014.
 */
class Float2(var x: Float = 0f, var y: Float = 0f) extends MetricObject[Float2] with HalfPlaneObject {

  def this(x: Double, y: Double) = this(x.toFloat, y.toFloat)
  def apply(x: Float = this.x, y: Float = this.y): Float2 = { this.x = x; this.y = y; this }

  def unary_- = Float2(-x, -y)

  override def equals(that: Any): Boolean = {
    val b = that.asInstanceOf[Float2]
    this.x == b.x && this.y == b.y
  }

  override def hashCode(): Int = 23*java.lang.Float.floatToIntBits(x) + java.lang.Float.floatToIntBits(y)


  def ~=(that: Float2, toleranceSq: Float = 0.0000000001f): Boolean = this.distanceSq(that) <= toleranceSq


  def +(that: Float2): Float2 = Float2(x + that.x, y + that.y)
  def -(that: Float2): Float2 = Float2(x - that.x, y - that.y)
  def *(that: Float2): Float2 = Float2(x * that.x, y * that.y)
  def /(that: Float2): Float2 = Float2(x / that.x, y / that.y)

  def +(c: Float): Float2 = Float2(x + c, y + c)
  def -(c: Float): Float2 = Float2(x - c, y - c)
  def *(c: Float): Float2 = Float2(c*x, c*y)
  def /(c: Float): Float2 = {val cc = 1f/c; Float2(x*cc, y*cc)}

  def +=(that: Float2): Unit = {x += that.x; y += that.y}
  def -=(that: Float2): Unit = {x -= that.x; y -= that.y}
  def *=(that: Float2): Unit = {x *= that.x; y *= that.y}
  def /=(that: Float2): Unit = {x /= that.x; y /= that.y}

  def +=(c: Float): Float2 = {x += c; y += c; this}
  def -=(c: Float): Float2 = {x -= c; y -= c; this}
  def *=(c: Float): Float2 = {x *= c; y *= c; this}
  def /=(c: Float): Float2 = {x /= c; y /= c; this}

  def rotate(angle: Float): Float2 = {
    val s = sin(angle)
    val c = cos(angle)
    val xx = x*c - y*s
    val yy = x*s + y*c
    x = xx; y = yy
    this
  }

  def rotated(angle: Float): Float2 = Float2(this).rotate(angle)

  def dot(that: Float2): Float = x*that.x + y*that.y
  def perpDot(that: Float2): Float = y*that.x - x*that.y
  def normal: Float2 = Float2(y, -x)
  def unit: Float2 = this/this.length
  def lengthSq: Float = x*x + y*y
  def length: Float = sqrt(lengthSq)

  def distanceSq(that: Float2): Float = Float2.distanceSq(this, that)

  def direction: Float = atan2(y, x)
  def manhattan: Float = abs(x + y)

  override def pointDeepestInHalfPlane(normal: Float2) = this

  def clamp(floor: Float2, ceil: Float2) = Float2(com.scilari.math.clamp(x, floor.x, ceil.x), com.scilari.math.clamp(y, floor.y, ceil.y))

  def copy = Float2(this)
  override def clone: Float2 = copy
  def toArray = Array(x, y)
  def toDoubleArray = Array(x.toDouble, y.toDouble)
  def toIntArray = Array(x.toInt, y.toInt)

  override def toString: String = "[" + x + ", " + y + "]"

}

object Float2{
  def apply(x: Float, y: Float) = new Float2(x, y)
  def apply(that: Float2): Float2 = Float2(that.x, that.y)
  def apply(xy: Float) = new Float2(xy, xy)
  def apply(x: Double, y: Double) = new Float2(x.toFloat, y.toFloat)
  val zero = Float2(0f, 0f)
  val one = Float2(1f, 1f)
  val nan = Float2(Float.NaN, Float.NaN)
  val inf = Float2(Float.PositiveInfinity, Float.PositiveInfinity)
  val unitX = Float2(1f, 0f)
  val unitY = Float2(0f, 1f)

  def max(p1: Float2, p2: Float2) = Float2(com.scilari.math.max(p1.x, p2.x), com.scilari.math.max(p1.y, p2.y))
  def min(p1: Float2, p2: Float2) = Float2(com.scilari.math.min(p1.x, p2.x), com.scilari.math.min(p1.y, p2.y))

  def toArray(f: Float2): Array[Double] = f.toDoubleArray
  def fromTuple(t: (Float, Float)) = Float2(t._1, t._2)

  def fromArray(a: Array[Float]) = Float2(a(0), a(1))
  def fromDoubleArray(a: Array[Double]) = Float2(a(0).toFloat, a(1).toFloat)

  def random: Float2 = randomZeroToOne
  def randomZeroToOne = Float2(scala.util.Random.nextFloat(), scala.util.Random.nextFloat())
  def randomMinusOneToOne: Float2 = Float2(1f) - Float2.random*2f
  def directed(angle: Float, length: Float = 1f) = Float2(Math.cos(angle).toFloat*length, Math.sin(angle).toFloat*length)
  def random(minX: Float, minY: Float, maxX: Float, maxY: Float): Float2 = Float2(minX, minY) + Float2.random*Float2(maxX - minX, maxY - minY)

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
  def sortByAngle[E <: Float2](queryPoint: Float2, points: Array[E]): Array[E] = {
    val q = queryPoint
    def isUp(p: Float2): Boolean = p.y - q.y > 0f || (p.y - q.y == 0f && p.x - q.y > 0f)
    def angleComparator (p1: Float2, p2: Float2): Boolean = {
      val u1 = isUp(p1)
      val u2 = isUp(p2)
      (u1 && !u2) || (u1 == u2 &&  (p1-q).perpDot(p2-q) > 0f)
    }

    object AngleOrdering extends Ordering[E] {
      def compare(p1: E, p2: E): Int = {
        if (angleComparator(p1, p2)) -1 else if(angleComparator(p2, p1)) 1 else 0
      }
    }

    scala.util.Sorting.quickSort(points)(AngleOrdering)
    points
  }

  def distanceSq(a: Float2, b: Float2): Float = { val dx = a.x - b.x; val dy = a.y - b.y; dx*dx + dy*dy}
  def distance(a: Float2, b: Float2): Float = Math.sqrt(distanceSq(a, b)).toFloat
  def angleBetween(a: Float2, b: Float2): Float = {
    Math.acos(cosBetween(a, b)).toFloat
  }

  def cosBetween(a: Float2, b: Float2): Float ={
    a.dot(b)*com.scilari.math.invSqrt(a.lengthSq*b.lengthSq)
  }

  implicit def Float2asDataPoint(x: Float2): DataPoint[Unit] = new DataPoint[Unit](0, 0, ())

}
