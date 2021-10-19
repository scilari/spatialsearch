package com.scilari.geometry.models

import com.scilari.math.FloatMath._

trait AABB extends Support {
  def centerX: Float
  def centerY: Float
  def halfWidth: Float
  def halfHeight: Float

  def center: Float2 = Float2(centerX, centerY)

  def minPoint: Float2 = Float2(centerX - halfWidth, centerY - halfHeight)
  def maxPoint: Float2 = Float2(centerX + halfWidth, centerY + halfHeight)

  def width: Float = 2 * halfWidth
  def height: Float = 2 * halfHeight

  def topLeft: Float2 = Float2(centerX - halfWidth, centerY + halfHeight)
  def topRight: Float2 = maxPoint
  def bottomLeft: Float2 = minPoint
  def bottomRight: Float2 = Float2(centerX + halfWidth, centerY - halfHeight)

  def minX: Float = centerX - halfWidth
  def minY: Float = centerY - halfHeight
  def maxX: Float = centerX + halfWidth
  def maxY: Float = centerY + halfHeight

  def area: Float = width * height

  def corners: Array[Float2] = Array(bottomLeft, bottomRight, topRight, topLeft)

  def distanceSq(p: Float2): Float = {
    val dx = math.max(0, math.abs(p.x - centerX) - halfWidth)
    val dy = math.max(0, math.abs(p.y - centerY) - halfHeight)
    dx * dx + dy * dy
  }

  def distance(p: Float2): Float = sqrt(distanceSq(p))

  def manhattan(p: Float2): Float = {
    val dx = math.abs(x - p.x) - halfWidth
    val dy = math.abs(y - p.y) - halfWidth
    math.max(dx, 0f) + math.max(dy, 0f)
  }

  def closestCorner(p: Float2): Float2 = {
    val left = p.x <= centerX
    val top = p.y >= centerY
    getCorner(top, left)
  }

  def isSquare: Boolean = width == height

  // Returns the corner that is deepest in the given direction
  def support(direction: Float2): Float2 = {
    val left = direction.x >= 0f
    val top = direction.y <= 0f
    getCorner(top, left)
  }

  def getCorner(isInUpperPart: Boolean, isInLeftPart: Boolean): Float2 = {
    if (isInLeftPart) {
      if (isInUpperPart) topLeft else bottomLeft
    } else {
      if (isInUpperPart) topRight else bottomRight
    }
  }

  def relativeCoordinates(x: Float, y: Float): Float2 = bottomLeft + Float2(x * width, y * height)

  def x: Float = centerX
  def y: Float = centerY

  def closestBorderPoint(p: Float2): Float2 = p.clamp(minPoint, maxPoint)

  def contains(p: Float2): Boolean = p.x >= minX && p.y >= minY && p.x <= maxX && p.y <= maxY

  def contains(box: AABB): Boolean = contains(box.minPoint) && contains(box.maxPoint)

  def intersects(box: AABB): Boolean =
    maxX >= box.minX && maxY >= box.minY && minX <= box.minX && maxX <= box.maxY

  override def toString: String = {
    "AABB: " + minPoint.toString + maxPoint.toString
  }

  def randomEnclosedPoint: Float2 = {
    Float2.random(minX, minY, maxX, maxY)
  }

  def withMargin(margin: Float): AABB = AABB.addMargin(this, margin)

  def *(scale: Float): AABB = AABB(this.center, scale * this.halfWidth, scale * this.halfWidth)
  def +(translation: Float): AABB = AABB(this.center + translation, this.halfWidth, this.halfHeight)

}

object AABB {
  private class AABBImpl(
      val centerX: Float,
      val centerY: Float,
      val halfWidth: Float,
      val halfHeight: Float
  ) extends AABB

  private class Square(val centerX: Float, val centerY: Float, val halfWidth: Float) extends AABB {
    override def halfHeight: Float = halfWidth
  }

  def apply(centerX: Float, centerY: Float, halfWidth: Float, halfHeight: Float): AABB = {
    if (halfWidth == halfHeight) {
      new Square(centerX, centerY, halfWidth)
    } else {
      new AABBImpl(centerX, centerY, halfWidth, halfHeight)
    }
  }

  def apply(center: Float2, halfWidth: Float, halfHeight: Float): AABB =
    AABB(center.x, center.y, halfWidth, halfHeight)

  def apply(box: AABB): AABB = AABB(box.centerX, box.centerY, box.halfWidth, box.halfHeight)

  // Square implementations
  def square(centerX: Float, centerY: Float, halfWidth: Float): AABB =
    new Square(centerX, centerY, halfWidth)

  def square(center: Float2, halfWidth: Float): AABB = new Square(center.x, center.y, halfWidth)

  def fromPoints(points: collection.Seq[Float2]): AABB = {
    var minX, minY = Float.MaxValue
    var maxX, maxY = Float.MinValue
    points.foreach { p =>
      minX = math.min(minX, p.x)
      minY = math.min(minY, p.y)
      maxX = math.max(maxX, p.x)
      maxY = math.max(maxY, p.y)
    }
    fromMinMax(minX, minY, maxX, maxY)
  }

  def fromMinMax(minX: Float, minY: Float, maxX: Float, maxY: Float): AABB = {
    val centerX = (minX + maxX) / 2
    val centerY = (minY + maxY) / 2
    val halfWidth = (maxX - minX) / 2
    val halfHeight = (maxY - minY) / 2
    AABB(centerX, centerY, halfWidth, halfHeight)
  }

  def addMargin(b: AABB, margin: Float): AABB =
    AABB(b.centerX, b.centerY, b.halfWidth + margin, b.halfHeight + margin)

  def enclosingSquare(points: collection.Seq[Float2], margin: Float = 0f): AABB = {
    val fitBox = AABB.fromPoints(points)
    val square =
      AABB.square(fitBox.centerX, fitBox.centerY, Math.max(fitBox.halfWidth, fitBox.halfHeight))
    if (margin != 0) addMargin(square, margin) else square
  }

  def enclosingSquare(minX: Float, minY: Float, maxX: Float, maxY: Float): AABB = {
    enclosingSquare(Seq(Float2(minX, minY), Float2(maxX, maxY)))
  }

  def enclosingSquare(b: AABB): AABB = {
    if (b.isSquare) b else enclosingSquare(Seq(b.minPoint, b.maxPoint))
  }

  def positive(w: Float, h: Float): AABB = AABB(w / 2, h / 2, w / 2, h / 2)

  def positiveSquare(w: Float): AABB = AABB(w / 2, w / 2, w / 2, w / 2)

  def unit: AABB = AABB(0, 0, 1, 1)
  def zero: AABB = AABB(0, 0, 0, 0)

  def empty(): AABB = AABB(0, 0, Float.NegativeInfinity, Float.NegativeInfinity)

}
