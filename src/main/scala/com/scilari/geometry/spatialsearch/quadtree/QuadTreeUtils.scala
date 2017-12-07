package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}

object QuadTreeUtils {
    val topLeftIndex: Int = 0
    val topRightIndex: Int = 1
    val bottomLeftIndex: Int = 2
    val bottomRightIndex: Int = 3


    def topLeftAABB(b: AABB): AABB = AABB(b.minPoint.x, b.centerY, b.centerX, b.maxPoint.y)

    def topRightAABB(b: AABB): AABB = AABB(b.center, b.maxPoint)

    def bottomLeftAABB(b: AABB): AABB = AABB(b.minPoint, b.center)

    def bottomRightAABB(b: AABB): AABB = AABB(b.centerX, b.minPoint.y, b.maxPoint.x, b.centerY)

    def topLeftAABB(b: AABB, centerPoint: Float2): AABB = AABB(b.minPoint.x, centerPoint.y, centerPoint.x, b.maxPoint.y)

    def topRightAABB(b: AABB, centerPoint: Float2): AABB = AABB(centerPoint, b.maxPoint)

    def bottomLeftAABB(b: AABB, centerPoint: Float2): AABB = AABB(b.minPoint, centerPoint)

    def bottomRightAABB(b: AABB, centerPoint: Float2): AABB = AABB(centerPoint.x, b.minPoint.y, b.maxPoint.x, centerPoint.y)

    def quadrantByIndex(b: AABB, index: Int): AABB = {
      index match {
        case `topLeftIndex` => topLeftAABB(b)
        case `topRightIndex` => topRightAABB(b)
        case `bottomLeftIndex` => bottomLeftAABB(b)
        case `bottomRightIndex` => bottomRightAABB(b)
      }
    }

    // Computes the AABB that has b as its quadrant and that expands most towards the given point
    def expandAABB(point: Float2, b: AABB): AABB = {
      val corner = b.closestCorner(point)
      new AABB(center = corner, halfWidth = b.width)
    }

    def findQuadrant(point: Float2, centerX: Float, centerY: Float): Int = {
      val rowIncrement = if (point.x <= centerX) 0 else 1
      val columnIncrement = if (point.y <= centerY) 2 else 0
      rowIncrement + columnIncrement
    }

    def findQuadrant(point: Float2, b: AABB): Int = findQuadrant(point, b.centerX, b.centerY)

    def findQuadrant(point: Float2, centerPoint: Float2): Int = findQuadrant(point, centerPoint.x, centerPoint.y)

  }
