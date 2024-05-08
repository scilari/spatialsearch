package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.quadtree.UnionTree
import scala.collection.mutable.ArraySeq

class UnionTreeTest extends SearchableTests with SearchableBase {
  override def treeName = "MultiTree"

  override def createPointTree: UnionTree[Float2] = {
    val ps1 = points.filter(p => p.x < p.y)
    val ps2 = points.filter(p => p.x >= p.y)
    val tree1 = QuadTree(AABB.unit, ps1)
    val tree2 = QuadTree(AABB.unit, ps2)
    UnionTree.fromTrees(List(tree1, tree2))
  }

  override def createCityTree: UnionTree[CityPoint] = {
    val d1 = cityData.filter { d => d.data.name.size < 5 }
    val d2 = cityData.filter { d => d.data.name.size >= 5 }
    val tree1 = QuadTree(d1)
    val tree2 = QuadTree(d2)
    UnionTree.fromTrees(List(tree1, tree2))
  }

}
