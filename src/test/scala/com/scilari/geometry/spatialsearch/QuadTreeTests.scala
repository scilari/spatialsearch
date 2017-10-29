package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.SearchTree.Concrete
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}

class QuadTreeTests extends TreeTests {
  override def treeName: String = "QuadTree"
  override def createEmptyUnitTree: Concrete[Float2] = QuadTree[Float2](AABB.unit, Parameters(nodeElementCapacity = 16))
  override def createFilledTree: Concrete[Float2] = QuadTree(points, Parameters(nodeElementCapacity = 16))
  override def createCityTree: Concrete[DataPoint[City]] = QuadTree[DataPoint[City]](cityData, Parameters(nodeElementCapacity = 16))
}
