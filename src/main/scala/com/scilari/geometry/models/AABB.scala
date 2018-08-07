package com.scilari.geometry.models


/**
  * Axis-aligned bounding box
 * Created by iv on 25.2.2014.
 */

class AABB private (var minPoint: Float2, var maxPoint: Float2) extends ExtremePoint {
  def this(minX: Float, minY: Float, maxX: Float, maxY: Float) = this(Float2(minX, minY), Float2(maxX, maxY))
  def this(box: AABB) = this(box.minPoint, box.maxPoint)
  def this(center: Float2, halfWidth: Float) = this(center - Float2(halfWidth), center + Float2(halfWidth))
  def set(box: AABB): Unit = { minPoint = box.minPoint; maxPoint = box.maxPoint }

  def width: Float = maxPoint.x - minPoint.x
  def height: Float = maxPoint.y - minPoint.y
  def centerX: Float = (minPoint.x + maxPoint.x)/2
  def centerY: Float = (minPoint.y + maxPoint.y)/2
  def center: Float2 = Float2(centerX, centerY)

  def topLeft: Float2 = Float2(minPoint.x, maxPoint.y)
  def topRight: Float2 = Float2(maxPoint)
  def bottomLeft: Float2 = Float2(minPoint)
  def bottomRight: Float2 = Float2(maxPoint.x, minPoint.y)

  def minX: Float = minPoint.x
  def minY: Float = minPoint.y
  def maxX: Float = maxPoint.x
  def maxY: Float = maxPoint.y

  def area: Float = width * height

  def corners: Array[Float2] = Array(topLeft, topRight, bottomLeft, bottomRight)

  def distanceSq(p: Float2): Float = {
    // Float2.distanceSq(p, closestBorderPoint(p)) // written open to avoid object creation
    val borderX = com.scilari.math.clamp(p.x, minX, maxX)
    val borderY = com.scilari.math.clamp(p.y, minY, maxY)
    val dx = borderX - p.x
    val dy = borderY - p.y
    dx*dx + dy*dy
  }

  def distance(p: Float2): Float = com.scilari.math.sqrt(distanceSq(p))

  def closestCorner(p: Float2): Float2 = {
    val left = p.x <= centerX
    val top = p.y >= centerY
    getCorner(top, left)
  }

  def isSquare: Boolean = width == height

  // Returns the corner that is deepest in the given direction
  def extremePoint(direction: Float2): Float2 = {
    val left = direction.x >= 0f
    val top = direction.y <= 0f
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

  def enclose(p: Float2): AABB = { minPoint = Float2.min(minPoint, p); maxPoint = Float2.max(maxPoint, p); this }
  def enclose(ps: Seq[Float2]): AABB = { ps.foreach(enclose); this }

  def enlarge(margin: Float): AABB = { minPoint -= margin; maxPoint += margin; this}

  def contains(p: Float2): Boolean = p.x >= minX && p.y >= minY && p.x <= maxX && p.y <= maxY

  def contains(box: AABB): Boolean = contains(box.minPoint) && contains(box.maxPoint)

  def intersects(box: AABB): Boolean = maxX >= box.minX && maxY >= box.minY && minX <= box.minX && maxX <= box.maxY


  def +(p: Float2): AABB = AABB(minPoint + p, maxPoint + p)
  def -(p: Float2): AABB = AABB(minPoint - p, maxPoint - p)

  override def toString: String = {
     "AABB: " + minPoint.toString + maxPoint.toString
  }

  def randomEnclosedPoint: Float2 = {
      Float2.random(minX, minY, maxX, maxY)
  }

}

object AABB{
  def apply(): AABB = empty()
  def apply(minPoint: Float2, maxPoint: Float2): AABB = {
    val b = new AABB(minPoint, maxPoint)
    require(b.area >= 0f, s"AABB area must be non-negative. Check corners, minPoint: $minPoint, maxPoint: $maxPoint")
    b
  }

  def apply(minX: Float, minY: Float, maxX: Float, maxY: Float): AABB = new AABB(minX, minY, maxX, maxY)
  def apply(box: AABB): AABB = AABB(box.minPoint, box.maxPoint)
  def apply(scale: Float): AABB = apply(Float2.zero, Float2(scale))

  def apply(points: Seq[Float2], margin: Float = 0f): AABB = {
    val b = empty()
    b.enclose(points)
    b.enlarge(margin)
  }

  def enclosingSquare(points: Seq[Float2], margin: Float = 0f): AABB = {
    val fitBox = AABB.apply(points, margin)
    new AABB(center = fitBox.center, halfWidth = Math.max(fitBox.width, fitBox.height)/2)
  }

  def enclosingSquare(minX: Float, minY: Float, maxX: Float, maxY: Float): AABB ={
    enclosingSquare(Seq(Float2(minX, minY), Float2(maxX, maxY)))
  }

  def enclosingSquare(b: AABB): AABB = {
    if(b.isSquare) b else enclosingSquare(Seq(b.minPoint, b.maxPoint))
  }

  val unit: AABB = AABB(0, 0, 1, 1)
  def zero: AABB = AABB(0, 0, 0, 0)
  def random: AABB = AABB(Float2.zero, Float2.random)

  def empty(): AABB = new AABB(Float2.inf, -Float2.inf)

}
