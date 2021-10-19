package com.scilari.geometry.models

/**
 * Data point for holding two-dimensional data
 */
case class DataPoint[E](position: Float2, data: E) extends Position

object DataPoint{
  def apply[E](x: Float, y: Float, data: E): DataPoint[E] = DataPoint(Float2(x, y), data)
}
