package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

class CompressedQuadTreeTest extends QuadTreeTests {
  override def treeName = "Compressed QuadTree"
  override def createPointTree: QuadTree[Float2] = {
    val bb = AABB.unit
    val tree = QuadTree(AABB.unit, points)
    println(s"Before compression: nodes = ${tree.root.nodes.size}, nodeArea = ${tree.root.nodes.map { _.bounds.area }.sum}")
    tree.root.compress()
    println(s"After compression: nodes = ${tree.root.nodes.size}, nodeArea = ${tree.root.nodes.map { _.bounds.area }.sum}")
    tree
  }

  override def createCityTree: QuadTree[DataPoint[City]] = {
    val tree = QuadTree(cityData)
    println(s"Before compression: nodes = ${tree.root.nodes.size}, nodeArea = ${tree.root.nodes.map { _.bounds.area }.sum}")
    tree.root.compress()
    println(s"After compression: nodes = ${tree.root.nodes.size}, nodeArea = ${tree.root.nodes.map { _.bounds.area }.sum}")
    tree
  }

}
