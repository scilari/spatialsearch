package com.scilari.geometry.models

/**
 * Created by iv on 8/15/2015.
 */
class DataPoint[E](x: Float, y: Float, var data: E = null) extends Float2(x, y) {
  def this(p: Float2, data: E) = this(p.x, p.y, data)
}
