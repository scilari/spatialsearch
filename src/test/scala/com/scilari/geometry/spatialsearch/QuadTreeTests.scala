package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.{City, cityData}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

class QuadTreeTests extends SearchableTests with SpatialContainerTests {
  override type TreeType = QuadTree[Float2]
  def treeName: String = "QuadTree"

  def createEmptyUnitTree: QuadTree[Float2] = QuadTree(AABB.unit)
  
  def createFilledTree: QuadTree[Float2] = {
    val bb = AABB.unit
    QuadTree(AABB.unit, points)
  }
  
  def createCityTree: QuadTree[DataPoint[City]] = QuadTree(cityData)

  treeName should "find same elements using in-node brute-force + search excluding the node" in {
    for(queryPoint <- queryPoints) {
      val r0 = 0
      val leaves = filledTree.rangeSearchLeaves(queryPoint, r0)
      if (treeName == "QuadTree") {
        leaves should have size 1
      }
      
      if (leaves.nonEmpty) {
        val leaf = leaves(0)
        val r1 = 0.2f
        val elemsInLeaf = leaf.elements.filter{e => e.position.distance(queryPoint) < r1}
        val elemsOutsideNode = filledTree.rangeExcludeNode(queryPoint, r1, leaf)
        val normalSearchResult = filledTree.rangeSearch(queryPoint, r1)
        elemsInLeaf ++ elemsOutsideNode should contain theSameElementsAs(normalSearchResult)
      }
      
    }
  }
}
