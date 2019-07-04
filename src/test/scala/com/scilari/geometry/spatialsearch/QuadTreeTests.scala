package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree

class QuadTreeTests extends SearchableTests with SpatialContainerTests {
  def createEmptyUnitTree: SearchableContainer[Float2] = QuadTree(AABB.unit)
  def createFilledTree: SearchableContainer[Float2] = QuadTree(points)
  def createCityTree: SearchableContainer[DataPoint[City]] = QuadTree(cityData)

  def treeName: String = "QuadTree"
}
