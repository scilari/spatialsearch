package com.scilari.geometry.models

/**
  * Data point for holding two-dimensional data
  * Created by iv on 8/15/2015.
 */
class DataPoint[E](x: Float, y: Float, var data: E = null) extends Float2(x, y) {
  def this(p: Float2, data: E) = this(p.x, p.y, data)

  override def equals(that: Any): Boolean ={
    isInstanceOf[DataPoint[E]] && {
      val thatDp = that.asInstanceOf[DataPoint[E]]
      equalCoordinates(thatDp) && data == thatDp.data
    }
  }

  override def hashCode(): Int = 37*super.hashCode() + data.hashCode()
}

object DataPoint{
  def apply[E](x: Float, y: Float, data: E): DataPoint[E] = new DataPoint[E](x, y, data)
  def apply[E](p: Float2, data: E): DataPoint[E] = new DataPoint[E](p, data)
}