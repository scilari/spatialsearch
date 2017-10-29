package com.scilari.geometry.models

/**
  * An object for which one can calculate a distance from a point
  * @tparam T The point type
  */
trait MetricObject[T] {
  def distanceSq(point: T): Float
  def distance(point: T): Float = math.sqrt(distanceSq(point)).toFloat
  def zeroDistance(point: T): Boolean = distanceSq(point) == 0f
}
