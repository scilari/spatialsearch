package com.scilari.geometry.models


/**
  * Axis-aligned bounding box
 * Created by iv on 25.2.2014.
 */

class AABB( var minPoint: Float2, var maxPoint: Float2 ) extends MetricObject[Float2] with HalfPlaneObject{

  def this(minX: Float, minY: Float, maxX: Float, maxY: Float) = this(Float2(minX, minY), Float2(maxX, maxY))
  def this(box: AABB) = this(box.minPoint, box.maxPoint)
  def this(center: Float2, halfWidth: Float) = this(center - Float2(halfWidth), center + Float2(halfWidth))
  def set(box: AABB): Unit = { minPoint = box.minPoint; maxPoint = box.maxPoint }

  def width: Float = maxPoint.x - minPoint.x
  def height: Float = maxPoint.y - minPoint.y
  def centerX: Float = (minPoint.x + maxPoint.x)/2
  def centerY: Float = (minPoint.y + maxPoint.y)/2
  def center: Float2 = Float2(centerX, centerY)

  def topLeft = Float2(minPoint.x, maxPoint.y)
  def topRight = Float2(maxPoint)
  def bottomLeft = Float2(minPoint)
  def bottomRight = Float2(maxPoint.x, minPoint.y)

  def minX: Float = minPoint.x
  def minY: Float = minPoint.y
  def maxX: Float = maxPoint.x
  def maxY: Float = maxPoint.y

  def area: Float = width * height

  def corners = Array(topLeft, topRight, bottomLeft, bottomRight)

  def distanceSq(p: Float2): Float = Float2.distanceSq(p, closestBorderPoint(p))

  def closestCorner(p: Float2): Float2 = {
    val left = p.x <= centerX
    val top = p.y >= centerY
    getCorner(top, left)
  }

  def isSquare: Boolean = width == height

  // Returns the corner that is deepest inside the half-plane defined by the normal of the separating line
  def pointDeepestInHalfPlane(normal: Float2): Float2 = {
    val left = normal.x >= 0f
    val top = normal.y <= 0f
    getCorner(top, left)
  }

  def getCorner(isInUpperPart: Boolean, isInLeftPart: Boolean): Float2 = {
    if(isInLeftPart){
      if(isInUpperPart) topLeft else bottomLeft
    } else {
      if(isInUpperPart) topRight else bottomRight
    }
  }

  def x: Float = centerX
  def y: Float = centerY

  def closestBorderPoint(p: Float2): Float2 = p.clamp(minPoint, maxPoint)

  def enclose(p: Float2): AABB = { minPoint = Float2.min(minPoint, p); maxPoint = Float2.max(maxPoint, p); this}
  def enclose(ps: Seq[Float2]): AABB = { ps.foreach(enclose); this }

  def contains(p: Float2): Boolean = p.x >= minX && p.y >= minY && p.x <= maxX && p.y <= maxY

  def contains(box: AABB): Boolean = contains(box.minPoint) && contains(box.maxPoint)

  def intersects(box: AABB): Boolean = maxX >= box.minX && maxY >= box.minY && minX <= box.minX && maxX <= box.maxY


  def +(p: Float2) = AABB(minPoint + p, maxPoint + p)
  def -(p: Float2) = AABB(minPoint - p, maxPoint - p)

  override def toString: String = {
     "AABB: " + minPoint.toString + maxPoint.toString
  }

  def randomEnclosedPoint: Float2 = {
      Float2.random(minX, minY, maxX, maxY)
  }

}

object AABB{
  def apply(): AABB = empty()
  def apply(minPoint: Float2, maxPoint: Float2) = new AABB(minPoint, maxPoint)
  def apply(minX: Float, minY: Float, maxX: Float, maxY: Float) = new AABB(minX, minY, maxX, maxY)
  def apply(box: AABB): AABB = AABB(box.minPoint, box.maxPoint)
  def apply(scale: Float): AABB = apply(Float2.zero, Float2(scale))

  def apply(points: Seq[Float2], margin: Float = 0f): AABB = {
    val xs = points.map{_.x}
    val ys = points.map{_.y}
    val bottomLeft = Float2(xs.min, ys.min)
    val topRight =  Float2(xs.max, ys.max)
    apply(bottomLeft - Float2(margin), topRight + Float2(margin))
  }

  def EnclosingSquare(points: Seq[Float2], margin: Float = 0f): AABB = {
    val fitBox = AABB.apply(points, margin)
    new AABB(center = fitBox.center, halfWidth = Math.max(fitBox.width, fitBox.height)/2)
  }

  def EnclosingSquare(minX: Float, minY: Float, maxX: Float, maxY: Float): AABB ={
    EnclosingSquare(Seq(Float2(minX, minY), Float2(maxX, maxY)))
  }

  val unit: AABB = AABB(0, 0, 1, 1)
  def zero: AABB = AABB(0, 0, 0, 0)
  def random: AABB = AABB(Float2.zero, Float2.random)

  def empty(): AABB = AABB(Float2.inf, -Float2.inf)


}
