package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.trees.rtree.RTree
import TestResources._
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree

class RTreeTests extends TreeTests{
  override def treeName: String = "RTree"
  override def createEmptyUnitTree: SearchTree[Float2] = RTree[Float2]()
  override def createFilledTree: SearchTree[Float2] = RTree[Float2](points)
  override def createCityTree: SearchTree[DataPoint[TestResources.City]] = RTree(cityData)
}
