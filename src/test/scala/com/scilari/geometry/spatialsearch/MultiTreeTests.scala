package com.scilari.geometry.spatialsearch

import com.scilari.geometry.spatialsearch.trees.quadtree.{Parameters, QuadTree}
import org.scalatest.{FlatSpec, Matchers}
import TestResources._
import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.SearchTree.MultiTree
import com.scilari.geometry.spatialsearch.plotting.TreePlotter

class MultiTreeTests extends FlatSpec with Matchers{
  val bb: AABB = AABB(cityData)
  val knnK: Int = 10
  val range: Float = 0.25f*bb.width
  val (small, large) = cityData.partition(c => c.data.population < 50000)

  val parameters = Parameters(nodeElementCapacity = 8)
  val quadTree: QuadTree[DataPoint[City]] = QuadTree(cityData, parameters)
  val quadSmall: QuadTree[DataPoint[City]] = QuadTree(small, parameters)
  val quadLarge: QuadTree[DataPoint[City]] = QuadTree(large, parameters)
  val multiTree = MultiTree(Seq(quadSmall, quadLarge))

  TreePlotter.plot(quadTree, elemRadius = 10)
  TreePlotter.plot(quadSmall, elemRadius = 10)
  TreePlotter.plot(quadLarge, elemRadius = 10)

  val queryPoints: Seq[Float2] = Seq.fill(100)(bb.randomEnclosedPoint)

  "MultiTree" should "find the same knn elements as the corresponding single tree" in {
    for (q <- queryPoints) {
      val mKnn = multiTree.knnSearch(q, knnK)
      val sKnn = quadTree.knnSearch(q, knnK)

      mKnn.map {
        _.data.name
      } should contain theSameElementsAs sKnn.map {
        _.data.name
      }
    }
  }

  it should "find the same range elements as the corresponding single tree" in {
    for (q <- queryPoints) {
      val mRange = multiTree.rangeSearch(q, range)
      val sRange = quadTree.rangeSearch(q, range)

      mRange.map {
        _.data.name
      } should contain theSameElementsAs sRange.map {
        _.data.name
      }
    }
  }

  it should "find the same polygonal elements as the corresponding single tree" in {
    for (q <- queryPoints) {
      val mPoly = multiTree.polygonalSearch(q)
      val sPoly = quadTree.polygonalSearch(q)

      mPoly.map {
        _.data.name
      } should contain theSameElementsAs sPoly.map {
        _.data.name
      }
    }
  }

}
