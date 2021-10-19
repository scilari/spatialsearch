package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}

object QuadTreeUtils {
    val topLeftIndex: Int = 0
    val topRightIndex: Int = 1
    val bottomLeftIndex: Int = 2
    val bottomRightIndex: Int = 3
  
    // Create the quadrants using the old center point and new halfWidth
    def topLeftAABB(c: Float2, hhw: Float): AABB = AABB(c.x - hhw, c.y + hhw, hhw)

    def topRightAABB(c:Float2, hhw: Float): AABB = AABB(c.x + hhw, c.y + hhw, hhw)

    def bottomLeftAABB(c: Float2, hhw: Float): AABB = AABB(c.x - hhw, c.y - hhw, hhw)

    def bottomRightAABB(c: Float2, hhw: Float): AABB = AABB(c.x + hhw, c.y - hhw, hhw)
  
    def quadrantByIndex(b: AABB, index: Int): AABB = {
      val c = b.center
      val hhw = b.halfWidth/2
      index match {
        case `topLeftIndex` => topLeftAABB(c, hhw)
        case `topRightIndex` => topRightAABB(c, hhw)
        case `bottomLeftIndex` => bottomLeftAABB(c, hhw)
        case `bottomRightIndex` => bottomRightAABB(c, hhw)
      }
    }

    // Computes the AABB that has b as its quadrant and that expands most towards the given point
    def expandedAABB(point: Float2, b: AABB): AABB = {
      val corner = b.closestCorner(point)
      AABB(center = corner, halfWidth = b.width)
    }

    def findQuadrant(point: Float2, centerX: Float, centerY: Float): Int = {
      val rowIncrement = if (point.x <= centerX) 0 else 1
      val columnIncrement = if (point.y <= centerY) 2 else 0
      rowIncrement + columnIncrement
    }

    def findQuadrant(point: Float2, b: AABB): Int = findQuadrant(point, b.centerX, b.centerY)

    def findQuadrant(point: Float2, centerPoint: Float2): Int = findQuadrant(point, centerPoint.x, centerPoint.y)

  }
