package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.rtree.RTree
import TestResources._

class RTreeTests extends TreeTests{
  override def treeName: String = "RTree"
  override def createEmptyUnitTree: BoundedSearchTree[Float2] = RTree[Float2]()
  override def createFilledTree: BoundedSearchTree[Float2] = RTree[Float2](points)
  override def createCityTree: BoundedSearchTree[DataPoint[TestResources.City]] = RTree(cityData)
}
