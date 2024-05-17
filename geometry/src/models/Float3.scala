package com.scilari.geometry.models

import com.scilari.math.FloatMath.{sqrt, clamp => floatClamp}

/** Data structure representing a vector of three floating points
  */
class Float3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {

  def +(that: Float3): Float3 = Float3(x + that.x, y + that.y, z + that.z)
  def -(that: Float3): Float3 = Float3(x - that.x, y - that.y, z - that.z)
  def *(that: Float3): Float3 = Float3(x * that.x, y * that.y, z * that.z)
  def /(that: Float3): Float3 = Float3(x / that.x, y / that.y, z / that.z)

  def +=(that: Float3): Float3 = { x += that.x; y += that.y; z += that.z; this }
  def -=(that: Float3): Float3 = { x -= that.x; y -= that.y; z -= that.z; this }
  def *=(that: Float3): Float3 = { x *= that.x; y *= that.y; z *= that.z; this }
  def /=(that: Float3): Float3 = { x /= that.x; y /= that.y; z /= that.z; this }

  def +(c: Float): Float3 = Float3(x + c, y + c, z + c)
  def -(c: Float): Float3 = Float3(x - c, y - c, z - c)
  def *(c: Float): Float3 = Float3(x * c, y * c, z * c)
  def *(c: Double): Float3 = Float3(x * c, y * c, z * c)
  def /(c: Float): Float3 = { val cc = 1f / c; Float3(x * cc, y * cc, z * cc) }
  def *=(c: Float): Float3 = { x *= c; y *= c; z *= c; this }

  def length: Float = sqrt(lengthSq)
  def lengthSq: Float = x * x + y * y + z * z
  def distance(that: Float3): Float = (this - that).length

  def toArray: Array[Float] = Array(x, y, z)
  def toDoubleArray: Array[Double] = Array(x.toDouble, y.toDouble, z.toDouble)

  def copy: Float3 = Float3(this)

  def xy: Float2 = Float2(x, y)
  def yx: Float2 = Float2(y, x)
  def xz: Float2 = Float2(x, z)
  def zx: Float2 = Float2(z, x)
  def yz: Float2 = Float2(y, z)
  def zy: Float2 = Float2(z, y)

  def rotatedXY(a: Float): Float3 = {
    val rot = xy.rotated(a)
    Float3(rot.x, rot.y, z)
  }

  def clamp(floor: Float3, ceil: Float3): Float3 =
    new Float3(
      floatClamp(x, floor.x, ceil.x),
      floatClamp(y, floor.y, ceil.y),
      floatClamp(z, floor.z, ceil.z)
    )

  def clampNormalize(floor: Float3, ceil: Float3): Float3 =
    (clamp(floor, ceil) - floor) / (ceil - floor)

  override def toString: String = "Float3: [" + x + ", " + y + ", " + z + "]"
}

object Float3 {
  def apply(x: Float, y: Float, z: Float): Float3 = new Float3(x, y, z)
  def apply(x: Double, y: Double, z: Double): Float3 = Float3(x.toFloat, y.toFloat, z.toFloat)
  def apply(f: Float3): Float3 = Float3(f.x, f.y, f.z)
  def apply(value: Float): Float3 = Float3(value, value, value)
  def apply(a: Array[Float]): Float3 = Float3(a(0), a(1), a(2))
  def apply(a: Array[Double]): Float3 = Float3(a(0), a(1), a(2))
  def random: Float3 = Float3(
    scala.util.Random.nextFloat(),
    scala.util.Random.nextFloat(),
    scala.util.Random.nextFloat()
  )
  def randomMinusOneToOne: Float3 = Float3(1f) - Float3.random * 2f

  extension (x: Float) {
    def *(p: Float3): Float3 = p * x
  }

  extension (x: Double) {
    def *(p: Float3): Float3 = p * x
  }

  import scala.language.implicitConversions
  implicit def fromArray(a: Array[Float]): Float3 = Float3(a)
  implicit def fromTuple(t: (Float, Float, Float)): Float3 = Float3(t._1, t._2, t._3)
  def fromDoubleArray(a: Array[Double]): Float3 = Float3(a(0).toFloat, a(1).toFloat, a(2).toFloat)
}
