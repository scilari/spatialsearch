package com.scilari.geometry.models

import com.scilari.math.HalfPi

class Pose(xx: Float = 0f, yy: Float = 0f, var heading: Angle = Angle(0f)) extends Float2(xx, yy) {
  def +(that: Pose): Pose = Pose(super. + (that), heading + that.heading)
  def -(that: Pose): Pose = Pose(super. - (that), heading - that.heading)
  def +=(that: Pose): Unit = {
    super.+=(that)
    heading += that.heading
  }
  def -=(that: Pose): Unit = {
    super.-=(that)
    heading -= that.heading
  }

  def a: Float = heading.value

  def forward(d: Float): Pose = { this.+=( Float2.directed(heading, d) ); this }

  def strafe(d: Float): Pose = { this.+=( Float2.directed(heading + HalfPi, d)); this }

  override def rotate(a: Float): Pose = { heading  += a; this }

  def move(control: Pose): Pose = {
    forward(control.x)
    strafe(control.y)
    rotate(control.heading)
    this
  }

  def moved(control: Pose): Pose = {
    val copy = Pose(this)
    copy.move(control)
    copy
  }

  def moveTo(pose: Pose): Unit ={
    this.x = pose.x; this.y = pose.y; this.heading = pose.heading
  }

  override def copy: Pose = Pose(this)

  override def toArray: Array[Float] = Array(x, y, heading)

  override def toString: String = "Pose: " + Array(x, y, a).mkString("[", " ", "]")

}

object Pose{
  def apply(x: Float, y: Float, a: Angle): Pose = new Pose(x, y, a)
  def apply(x: Float, y: Float, angle: Float): Pose = this(x, y, Angle(angle))
  def apply(that: Pose): Pose = Pose(that.x, that.y, that.heading)
  def apply(float2: Float2, a: Float = 0f): Pose = Pose(float2.x, float2.y, a)
}

