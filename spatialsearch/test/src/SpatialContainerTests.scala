package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources._
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

trait SpatialContainerTests extends TreeTestBase {

  treeName should "be nonEmpty after adding points" in {
    val tree = createEmptyUnitTree
    assert(tree.isEmpty)
    points.foreach {
      tree.add
    }
    assert(tree.nonEmpty)
  }

  it should "be nonEmptyIfNotEmptied after adding points and also after removing all points" in {
    val tree = createEmptyUnitTree
    assert(tree.isEmpty)
    assert(!tree.nonEmptyIfNotEmptied)
    points.foreach {
      tree.add
    }
    assert(tree.nonEmptyIfNotEmptied)

    tree.remove(points)
    assert(tree.isEmpty)
    assert(tree.nonEmptyIfNotEmptied)
  }


  val cityTree = createCityTree

  it should "contain all elements" in {
    cityTree.size should be (cityData.size)
  }

  it should "contain large and small cities" in {
    val L = cityTree.elements.count(_.data.population > 50000)
    val S = cityTree.elements.count(_.data.population <= 50000)
    (L + S) should be (cityData.size)
    L should be (14)
  }

  it should "have removal functionality" in {
    val cityTree = createCityTree
    val toBeRemoved = cityData.take(3)
    for(e <- toBeRemoved){
      cityTree.remove(e)
    }

    val treeNames = cityTree.elements.toList.map{_.data.name}
    val names = cityData.drop(3).map{_.data.name}
    treeNames should contain theSameElementsAs names

  }

  it should "have simultaneous removal functionality" in {
    val cityTree = createCityTree
    val toBeRemoved = cityData.take(10)
    cityTree.remove(toBeRemoved)
    val treeNames = cityTree.elements.toList.map{_.data.name}
    val names = cityData.drop(10).map{_.data.name}
    treeNames should contain theSameElementsAs names
  }

}
