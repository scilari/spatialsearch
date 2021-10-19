package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.quadtree.MultiQuadTree
import scala.collection.mutable.ArraySeq

class MultiQuadTreeTest extends SearchableTests with SearchableBase {
  override def treeName = "MultiQuadTree"
  
  override def createPointTree: MultiQuadTree[Float2] = {
    val ps1 = points.filter(p => p.x < p.y)
    val ps2 = points.filter(p => p.x >= p.y)
    val tree1 = QuadTree(AABB.unit, ps1)
    val tree2 = QuadTree(AABB.unit, ps2)
    MultiQuadTree(ArraySeq(tree1.root, tree2.root))
  }
  
  override def createCityTree: MultiQuadTree[CityPoint] = {
    val d1 = cityData.filter{d => d.data.name.size < 5}
    val d2 = cityData.filter{d => d.data.name.size >= 5}
    val tree1 = QuadTree(d1)
    val tree2 = QuadTree(d2)
    MultiQuadTree(ArraySeq(tree1.root, tree2.root))
  }
  
}
