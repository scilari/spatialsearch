package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import org.scalacheck.Gen
import org.scalatest._
import flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks



object QuadTreeSpec {
  val minX: Float = -1000f
  val maxX: Float = 1000f
  val minY: Float = -1000f
  val maxY: Float = 1000f

  val pointCount: Int = 1000
  val minKnnPointCount: Int = 1
  val maxKnnPointCount: Int = 200

  val seqQueryPointCount = 20

  val genK: Gen[Int] = Gen.choose[Int](minKnnPointCount, maxKnnPointCount)

  val genRadius: Gen[Float] = Gen.choose[Float](0.00f, 500f)

  val bb = AABB(minX, minY, maxX, maxY)

  val genPoint: Gen[Float2] =
    for {
      x <- Gen.chooseNum[Float](minX, maxX)
      y <- Gen.chooseNum[Float](minY, maxY)
    } yield Float2(x, y)

  val genPoints: Gen[List[Float2]] = Gen.listOfN(pointCount, genPoint)
  val genDistinctPoints: Gen[Set[Float2]] = Gen.containerOfN[Set, Float2](pointCount, genPoint)

  val genSeqQueryPoints: Gen[List[Float2]] = Gen.listOfN(seqQueryPointCount, genPoint)

  val genTree: Gen[QuadTree[Float2]] =
    for {
      ps <- genDistinctPoints
    } yield {
      val tree = QuadTree(ps.toSeq)
      tree
    }
}


class QuadTreeSpec extends AnyFlatSpec with ScalaCheckPropertyChecks with Matchers{
  import QuadTreeSpec._
  
  "QuadTree" should "contain its elements" in {
    forAll(genPoints){ points =>
      val tree = QuadTree(points)
      val quadPoints = tree.elements
      quadPoints.toSet should be (points.toSet)
    }
  }

  "Knn search" should "return the k nearest neighbors" in {
    forAll(genTree) { tree =>
      val allPoints = tree.elements
      forAll(genPoint, genK){ (queryPoint, k) =>
        val knnPoints = tree.knnSearch(queryPoint, k).sortBy((point: Float2) => point.distanceSq(queryPoint))
        // Generate brute force data to compare to
        val boundaryDistance = knnPoints.last.distanceSq(queryPoint)

        val foundInsidePoints = knnPoints.filter(p => p.distanceSq(queryPoint) < boundaryDistance)
        val bruteInsidePoints = allPoints.filter(p => p.distanceSq(queryPoint) < boundaryDistance)

        assert(foundInsidePoints.toSet == bruteInsidePoints.toSet)

        val foundBoundaryPoints = knnPoints.filter(p => p.distanceSq(queryPoint) == boundaryDistance)
        val bruteBoundaryPoints = allPoints.filter(p => p.distanceSq(queryPoint) == boundaryDistance)
        
        assert(foundBoundaryPoints.toSet.subsetOf(bruteBoundaryPoints.toSet))
      }
    }
  }

  "Radius search" should "return the points inside the radius" in {
    forAll(genTree) { tree =>
      val allPoints = tree.elements
      forAll(genPoint, genRadius){ (queryPoint: Float2, r: Float) =>
        val queriedPoints = tree.rangeSearch(queryPoint, r).sortBy(_.distanceSq(queryPoint))
        // Generate brute force data to compare to
        val filteredPoints = allPoints.filter{_.distance(queryPoint) <= r }
        queriedPoints.toSet should equal (filteredPoints.toSet)
      }
    }
  }
  
  
}
