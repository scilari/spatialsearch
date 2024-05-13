package com.scilari.geometry.models

case class Circle(center: Float2, r: Float) extends Position {
  val r2 = r * r
  def position = center
  def contains(p: Float2) = center.distanceSq(p) <= r2

  def scale(scale: Float): Circle = Circle(scale * this.center, scale * this.r)
}
