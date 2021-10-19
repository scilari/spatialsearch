package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

class CompressedQuadTreeTest extends QuadTreeTests {
  override def treeName = "Compressed QuadTree"
  
  override def createFilledTree: QuadTree[Float2] = {
    val bb = AABB.unit
    val tree = QuadTree(AABB.unit, points)
    tree.root.compress()
    tree
  }
  
  override def createCityTree: QuadTree[DataPoint[City]] = {
    val tree = QuadTree(cityData)
    tree.root.compress()
    tree
  }

}
