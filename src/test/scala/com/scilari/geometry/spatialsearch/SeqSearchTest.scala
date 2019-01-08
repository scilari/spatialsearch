package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class SeqSearchTest extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  import QuadTreeSpec._

  property("Sequence knn search should find the same elements as brute force") {
    forAll(genTree) { tree =>
      val allPoints = tree.elements
      forAll(genSeqQueryPoints, genK){ (queryPoints: List[Float2], k: Int) =>
        def minDist(p: Float2): Float = queryPoints.map{ q => p.distanceSq(q) }.min

        val foundPoints = tree.seqKnnSearch(queryPoints.toIndexedSeq, k).sortBy(minDist)
        val sortedPoints = allPoints.sortBy(minDist)

        val boundaryPoint = sortedPoints(k-1)
        val boundaryDistance = queryPoints.map{_.distanceSq(boundaryPoint)}.min

        val foundInsidePoints = foundPoints.filter{ p => minDist(p) < boundaryDistance }
        val insidePoints = allPoints.filter{ p => minDist(p) < boundaryDistance }

        assert(foundInsidePoints.toSet == insidePoints.toSet)

        val foundBoundaryPoints = foundPoints.filter{ p => minDist(p) == boundaryDistance }
        val boundaryPoints = allPoints.filter{ p => minDist(p) == boundaryDistance }

        assert(foundBoundaryPoints.toSet.subsetOf(boundaryPoints.toSet))
      }
    }
  }

  property("Sequence radius search should find the same elements as regular search") {
    forAll(genTree) { tree =>
      val allPoints = tree.elements
      forAll(genPoints, genRadius){ (queryPoints: List[Float2], r: Float) =>

        val foundPoints = tree.seqRangeSearch(queryPoints.toIndexedSeq, r)
        val separatelyQueriedPoints = queryPoints.flatMap{ queryPoint =>
          tree.rangeSearch(queryPoint, r)
        }

        val filteredPoints = allPoints.filter{p => queryPoints.map{q => p.distance(q)}.min <= r }
        foundPoints.toSet == filteredPoints.toSet && foundPoints.toSet == separatelyQueriedPoints.toSet

      }
    }
  }



}
