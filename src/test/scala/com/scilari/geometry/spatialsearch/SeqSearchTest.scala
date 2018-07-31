package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class SeqSearchTest extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  import QuadTreeSpec._

  property("Sequence knn search should find the same elements as brute force") {
    forAll(genTree) { tree =>
      val allPoints = tree.elements
      forAll(genPoints, genK){ (queryPoints: List[Float2], k: Int) =>
        val queriedPoints = tree.seqKnnSearch(queryPoints.toIndexedSeq, k)

        val sortedPoints = allPoints.sortBy(p => queryPoints.map{q => p.distanceSq(q)}.min)
        val maxAcceptableDist = queryPoints.map{sortedPoints(k-1).distanceSq}.min
        val acceptablePoints = sortedPoints.filter{ p => queryPoints.map{ q => p.distanceSq(q) }.min <= maxAcceptableDist}

        assert(queriedPoints.toSet.subsetOf(acceptablePoints.toSet))

      }
    }
  }

  property("Sequence radius search should find the same elements as regular search") {
    forAll(genTree) { tree =>
      val allPoints = tree.elements
      forAll(genPoints, genRadius){ (queryPoints: List[Float2], r: Float) =>
        val queriedPoints = tree.seqRangeSearch(queryPoints.toIndexedSeq, r)
        val separatelyQueriedPoints = queryPoints.flatMap{ queryPoint =>
          tree.rangeSearch(queryPoint, r)
        }

        val filteredPoints = allPoints.filter{p => queryPoints.map{q => p.distance(q)}.min <= r }
        queriedPoints.toSet should equal (filteredPoints.toSet)

        queriedPoints.toSet should equal (separatelyQueriedPoints.toSet)

      }
    }
  }



}
