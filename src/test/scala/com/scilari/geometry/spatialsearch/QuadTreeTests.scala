package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}

class QuadTreeTests extends TreeTests {
  override def treeName: String = "QuadTree"
  override def createEmptyUnitTree: BoundedSearchTree[Float2] = QuadTree[Float2](AABB.unit, Parameters(nodeElementCapacity = 16))
  override def createFilledTree: BoundedSearchTree[Float2] = QuadTree(points, Parameters(nodeElementCapacity = 16))
  override def createCityTree: BoundedSearchTree[DataPoint[City]] = QuadTree[DataPoint[City]](cityData, Parameters(nodeElementCapacity = 16))
}
