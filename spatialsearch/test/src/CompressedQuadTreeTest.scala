package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

class CompressedQuadTreeTest extends QuadTreeTests {
  override def treeName = "Compressed QuadTree"
  override def createPointTree: QuadTree[Float2] = {
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

  treeName should "have smaller node area after compression" in {
    val bb = AABB.unit
    val tree = QuadTree(AABB.unit, points)
    val areaBefore = tree.root.nodes.map { _.bounds.area }.sum
    tree.root.compress()
    val areaAfter = tree.root.nodes.map { _.bounds.area }.sum
    assert(areaAfter < areaBefore, "Does not have smaller area")
    info(s"Node area reduction: ${areaBefore} -> ${areaAfter}")
  }

}
