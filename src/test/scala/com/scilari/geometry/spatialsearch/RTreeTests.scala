package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.rtree.RTree
import TestResources._

class RTreeTests extends TreeTests{
  override def treeName: String = "RTree"
  override def createEmptyUnitTree: SearchTree.Concrete[Float2] = RTree[Float2]()
  override def createFilledTree: SearchTree.Concrete[Float2] = RTree[Float2](points)
  override def createCityTree: SearchTree.Concrete[DataPoint[TestResources.City]] = RTree(cityData)
}
