package com.scilari.geometry.models

import com.scilari.math.FloatMath.{cos, sin, _}

/** Two-dimensional point represented by Float coordinates
  * @param x
  *   Coordinate x
  * @param y
  *   Coordinate y
  */
final case class Float2(x: Float = 0f, y: Float = 0f) extends Support with Position {
  def this(x: Double, y: Double) = this(x.toFloat, y.toFloat)

  def unary_- : Float2 = Float2(-x, -y)

  def +(that: Float2): Float2 = Float2(x + that.x, y + that.y)
  def -(that: Float2): Float2 = Float2(x - that.x, y - that.y)
  def *(that: Float2): Float2 = Float2(x * that.x, y * that.y)
  def /(that: Float2): Float2 = Float2(x / that.x, y / that.y)

  def +(c: Float): Float2 = Float2(x + c, y + c)
  def -(c: Float): Float2 = Float2(x - c, y - c)
  def *(c: Float): Float2 = Float2(c * x, c * y)
  def *(c: Double): Float2 = Float2(c * x, c * y)
  def /(c: Float): Float2 = { val cc = 1f / c; Float2(x * cc, y * cc) }

  def dot(that: Float2): Float = x * that.x + y * that.y
  def perpDot(that: Float2): Float = y * that.x - x * that.y
  def normalCW: Float2 = Float2(y, -x)
  def normalCCW: Float2 = Float2(-y, x)
  def perp: Float2 = normalCW
  def perpCCW: Float2 = normalCCW
  def perpCW(s: Float): Float2 = Float2(s * y, -s * x)
  def perpCCW(s: Float): Float2 = Float2(-s * y, s * x)
  def unit: Float2 = this / this.length
  def lengthSq: Float = x * x + y * y
  def length: Float = sqrt(lengthSq)

  def distanceSq(that: Float2): Float = Float2.distanceSq(this, that)
  def distance(that: Float2): Float = sqrt(distanceSq(that))

  def zeroDistance(point: Float2): Boolean = equalCoordinates(point)

  def direction: Float = atan2(y, x)
  def manhattan: Float = abs(x) + abs(y)

  override def support(direction: Float2): Float2 = this

  override def position: Float2 = this

  def normalized: Float2 = this / length

  def rotated(angle: Float): Float2 = rotated(cos(angle), sin(angle))

  def rotatedX(cos: Float, sin: Float): Float = x * cos - y * sin

  def rotatedY(cos: Float, sin: Float): Float = x * sin + y * cos

  def rotated(cos: Float, sin: Float): Float2 = {
    Float2(rotatedX(cos, sin), rotatedY(cos, sin))
  }

  def clamp(floor: Float2, ceil: Float2): Float2 =
    Float2(
      com.scilari.math.FloatMath.clamp(x, floor.x, ceil.x),
      com.scilari.math.FloatMath.clamp(y, floor.y, ceil.y)
    )

  def toArray: Array[Float] = Array(x, y)
  def toDoubleArray: Array[Double] = Array(x.toDouble, y.toDouble)
  def toIntArray: Array[Int] = Array[Int](x.toInt, y.toInt)

  def ~=(that: Float2, toleranceSq: Float = 0.000001f): Boolean =
    this.distanceSq(that) <= toleranceSq
  def equalCoordinates(that: Float2): Boolean = x == that.x && y == that.y

  override def toString: String = s"[$x, $y]"

}

object Float2 {
  def apply(x: Float, y: Float) = new Float2(x, y)
  def apply(that: Float2): Float2 = Float2(that.x, that.y)
  def apply(xy: Float): Float2 = new Float2(xy, xy)
  def apply(x: Double, y: Double): Float2 = new Float2(x.toFloat, y.toFloat)

  extension (x: Float) {
    def *(p: Float2): Float2 = p * x
  }

  extension (x: Double) {
    def *(p: Float2): Float2 = p * x
  }

  def zero: Float2 = Float2(0f, 0f)
  def one: Float2 = Float2(1f, 1f)
  def nan: Float2 = Float2(Float.NaN, Float.NaN)
  def inf: Float2 = Float2(Float.PositiveInfinity, Float.PositiveInfinity)
  def unitX: Float2 = Float2(1f, 0f)
  def unitY: Float2 = Float2(0f, 1f)

  def random: Float2 = randomZeroToOne
  def random(scale: Float): Float2 = random(Float2.zero, Float2(scale))
  def randomZeroToOne: Float2 = Float2(scala.util.Random.nextFloat(), scala.util.Random.nextFloat())
  def randomMinusOneToOne: Float2 = Float2(1f) - Float2.random * 2f
  def random(minX: Float, minY: Float, maxX: Float, maxY: Float): Float2 = {
    Float2(minX, minY) + Float2.random * Float2(maxX - minX, maxY - minY)
  }
  def random(minPoint: Float2, maxPoint: Float2): Float2 =
    random(minPoint.x, minPoint.y, maxPoint.x, maxPoint.y)

  def directed(angle: Float, length: Float = 1f): Float2 = {
    Float2(cos(angle).toFloat * length, sin(angle).toFloat * length)
  }

  def distanceSq(a: Float2, b: Float2): Float = {
    val dx = a.x - b.x; val dy = a.y - b.y; dx * dx + dy * dy
  }

  def distance(a: Float2, b: Float2): Float = sqrt(distanceSq(a, b))

  def manhattan(a: Float2, b: Float2): Float = abs(a.x - b.x) + abs(a.y - b.y)
}
