package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

object QuadTreeSpec {
  val minX: Float = -1000f
  val maxX: Float = 1000f
  val minY: Float = -1000f
  val maxY: Float = 1000f

  val pointCount: Int = 1000
  val minKnnPointCount: Int = 10
  val maxKnnPointCount: Int = 200

  val genK: Gen[Int] = Gen.choose[Int](minKnnPointCount, maxKnnPointCount)
  val genRadius: Gen[Float] = Gen.choose[Float](0f, 500f)

  val bb = AABB(minX, minY, maxX, maxY)

  val genPoint: Gen[Float2] =
    for {
      x <- Gen.chooseNum[Float](minX, maxX)
      y <- Gen.chooseNum[Float](minY, maxY)
    } yield Float2(x, y)

  val genPoints: Gen[List[Float2]] = Gen.listOfN(pointCount, genPoint)
  val genDistinctPoints: Gen[Set[Float2]] = Gen.containerOfN[Set, Float2](pointCount, genPoint)

  val genTree: Gen[QuadTree[Float2]] =
    for {
      ps <- genDistinctPoints
    } yield {
      val tree = QuadTree(ps.toSeq)
      tree
    }
}


class QuadTreeSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers{
  import QuadTreeSpec._

    property("Quadtree should contain its elements"){
      forAll(genPoints){points: Seq[Float2] =>
        val tree = QuadTree(points)
        val quadPoints = tree.elements
        quadPoints.toSet should be (points.toSet)
      }
    }

    property("Knn search should return the k nearest neighbors") {
      forAll(genTree) { tree =>
        val allPoints = tree.elements
        forAll(genPoint, genK){ (queryPoint, k) =>
          val knnPoints = tree.knnSearch(queryPoint, k).sortBy((point: Float2) => point.distanceSq(queryPoint))
          // Generate brute force data to compare to
          val sortedPoints = allPoints.sortBy(_.distanceSq(queryPoint))
          val maxAcceptableDist = sortedPoints(k-1).distanceSq(queryPoint)
          val acceptablePoints = sortedPoints.filter{ _.distanceSq(queryPoint) <= maxAcceptableDist}

          assert(knnPoints.toSet.subsetOf(acceptablePoints.toSet))

        }
      }
    }

    property("Radius search should return the points inside the radius") {
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
