// package com.scilari.geometry.performance

// import com.csdgn.util.KDTree
// import jk.tree.KDTree.Euclidean
// import com.scilari.geometry.models.Float2
// import com.scilari.geometry.utils.Float2Utils
// import com.scilari.geometry.spatialsearch.TestUtils.Timing._
// import com.scilari.geometry.spatialsearch.plotting.TreePlotter
// import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}
// import org.scalatest._
// import flatspec._
// import matchers._

// import scala.collection.mutable


// class QuadTreePerformanceTests extends AnyFlatSpec with should.Matchers {
//   import PerformanceBase._
  
  
//   def testInfo(): Unit ={
//     val areaBeforeCompression = quadTree.root.leaves.map{_.bounds.area}.sum
//     quadTree.root.compress()
//     val areaAfterCompresisons = quadTree.root.leaves.map{_.bounds.area}.sum
//     info(s"Compression: $areaBeforeCompression -> $areaAfterCompresisons")
//     if(plotting) TreePlotter.plot(quadTree, "QuadTree", elemRadius = bb.width / 500)
    

//     info("== Test info == ")
//     info("Point count: " + pointCount)
//     info("Run count: " + runCount)
//     info("Query count (per run): " + queryCount)
//     info("Insert run count: " + insertRunCount)
//     info("Knn k: " + queryK)
//     info("Range: " + range + " out of total point area of " + bb.width + " x " + bb.height)
//     info("Quadtree. depth: " +  quadTree.root.depth + " nodeCount: " + quadTree.root.nodes.size)
//     info("Quadtree. parameters: " +  quadTree.parameters)
//     info("Min node size: " + quadTree.root.leaves.map{_.bounds.width}.min)
//     info("================")
//   }

//   "QuadTree" should "have similar insertion performance to KDTree" in {
//     testInfo()

//     val result = compareTime(
//       "QuadTree",
//       {
//         val quadTree = QuadTree[Float2](bb)
//         points.foreach(quadTree.add)
//       },
//       "KDTree",
//       {
//         val kdTree = new KDTree[Float2](2, 48)
//         pointsArray.foreach(k => kdTree.add(k, f2))
//       },
//       insertRunCount
//     )

//     info("\n== Insert time == ")
//     info(result.toInfo(totalInsertCount))
//     assert(similarTime(result.millis1, result.millis2))
//   }

//   it should "have similar insertion performance to KDTree 2" in {
//     val result = compareTime(
//       "QuadTree",
//       {
//         val quadTree = QuadTree[Float2](bb)
//         points.foreach(quadTree.add)
//       },
//       "KDTree",
//       {
//         val kdTree = new Euclidean[Float2](2)
//         pointsArray.foreach(k => kdTree.addPoint(k, f2))
//       },
//       insertRunCount
//     )

//     info("\n== Insert time == ")
//     info(result.toInfo(totalInsertCount))
//     assert(similarTime(result.millis1, result.millis2))
//   }


//   it should "have similar knn query performance to KDTree" in {

//     val result = compareTime(
//       "QuadTree",
//       for(q <- queryPoints){
//         BlackHole.consumeAny(quadTree.knnSearch(q, queryK))
//       },
//       "KDTree",
//       for(q <- queryArray){
//         BlackHole.consumeAny(kdTree.getNearestNeighbors(q, queryK))
//       },
//       runCount
//     )

//     info("\n== Knn query time == ")
//     info(result.toInfo(totalQueryCount))

//     assert(similarTime(result.millis1, result.millis2))
//   }

//   it should "have similar knn query performance to KDTree 2" in {

//     val result = compareTime(
//       "QuadTree",
//       for(q <- queryPoints){
//         BlackHole.consumeAny(quadTree.knnSearch(q, queryK))
//       },
//       "KDTree",
//       for(q <- queryArray){
//         BlackHole.consumeAny(kdTree2.nearestNeighbours(q, queryK))
//       },
//       runCount
//     )

//     info("\n== Knn query time == ")
//     info(result.toInfo(totalQueryCount))

//     assert(similarTime(result.millis1, result.millis2))
//   }

//   it should "have similar range query performance to KDTree" in {
//     val result = compareTime(
//       "QuadTree",
//       for(q <- queryPoints){
//         BlackHole.consumeAny(quadTree.rangeSearch(q, range))
//       },
//       "KDTree",
//       for(i <- kdRangeLows.indices){
//         BlackHole.consumeAny(kdTree.getRange(kdRangeLows(i), kdRangeHighs(i)))
//       },
//       runCount
//     )

//     info("\n== Range query time ==")
//     info(result.toInfo(totalQueryCount))
//     assert(similarTime(result.millis1, result.millis2))
//   }

//   it should "have similar range query performance to KDTree 2" in {
//     val result = compareTime(
//       "QuadTree",
//       queryPoints.foreach{ q =>
//         BlackHole.consumeAny(quadTree.rangeSearch(q, range))
//       },
//       "KDTree 2",
//       queryArray.foreach{ q =>
//         BlackHole.consumeAny(kdTree2.ballSearch(q, range))
//       },
//       runCount
//     )
    
//     info("\n== Range query time ==")
//     info(result.toInfo(totalQueryCount))
//     assert(similarTime(result.millis1, result.millis2))
//   }

//   it should "have similar performance with polygonal search and range search with similar range" in {
//     // polygonal search improves with smaller nodeElementCapacity
//     val quadTree = QuadTree(points, Parameters(nodeElementCapacity = 15))

//     val maxRanges: Seq[Float] = for(q <- queryPoints) yield quadTree.polygonalSearch(q).map{_.distance(q)}.max
//     val queryPointsWithMaxRanges = queryPoints zip maxRanges
//     val meanRange = maxRanges.sum/maxRanges.size

//     val result = compareTime(
//       "QuadTree (polygonal)",
//       for(q <- queryPoints){
//         val neighbors = quadTree.polygonalSearch(q)
//       },
//       "QuadTree (mean range)",
//       for(q <- queryPoints){
//         val neighbors = quadTree.rangeSearch(q, meanRange)
//       },
//       runCount
//     )

//     val resultFast = compareTime(
//       "QuadTree (fast polygonal)",
//       for (q <- queryPoints) {
//         val neighbors = quadTree.fastPolygonalSearch(q)
//       },
//       "QuadTree (max range)",
//       for ((q, r) <- queryPointsWithMaxRanges) {
//         val neighbors = quadTree.rangeSearch(q, r)
//       },
//       runCount
//     )

//     //assert(result.millis1 > resultFast.millis1)

//     info("\n== Polygonal query times ==")
//     info(result.toInfo(totalQueryCount))
//     info(resultFast.toInfo(totalQueryCount))

//     //assert(similarOrBetterTime(resultFast.millis1, resultFast.millis2))
//   }
// //
// //  it should "have similar sequence range query performance than with separate queries" in {
// //    val slidingQueryPoints = queryPoints.sliding(20)
// //
// //    val tSeq = warmUpAndMeasureTime({
// //      for(qs <- slidingQueryPoints){
// //        BlackHole.consumeAny(quadTree.seqRangeSearch(qs, range))
// //      }
// //    }, runCount, warmUpCount)
// //
// //    val tSep = warmUpAndMeasureTime({
// //      for(qs <- slidingQueryPoints){
// //        for(q <- qs){
// //          BlackHole.consumeAny(quadTree.rangeSearch(q, range))
// //        }
// //      }
// //    }, runCount, warmUpCount)
// //
// //    val tSepSet = warmUpAndMeasureTime({
// //      for(qs <- slidingQueryPoints){
// //        val neighbors = for(q <- qs) yield {
// //          quadTree.rangeSearch(q, range)
// //        }
// //        BlackHole.consumeAny(neighbors.flatten.toSet)
// //      }
// //    }, runCount, warmUpCount)
// //
// //    info(s"\nSequence-based vs separate range query time: ${tSeq/tSep}")
// //    info(s"Sequence-based vs separate unique range query time: ${tSeq/tSepSet}")
// //    assert(similarTime(tSeq, tSep))
// //  }
// //
// //  it should "have similar path sequence range query performance than with separate queries" in {
// //    val range = 0.1f*bb.width
// //    val pathCount = 100
// //    for(pathPoints <- Seq(10, 50, 200)){
// //      val paths = (0 until pathCount ).map { _ =>
// //        Float2Utils.linSpace(bb.randomEnclosedPoint, bb.randomEnclosedPoint, pathPoints)
// //      }
// //
// //      val tSeq = warmUpAndMeasureTime({
// //        for(qs <- paths){
// //          BlackHole.consumeAny(quadTree.seqRangeSearch(qs, range))
// //        }
// //      }, runCount/pathCount*10, warmUpCount/pathCount*10)
// //
// //      val tSep = warmUpAndMeasureTime({
// //        for(qs <- paths){
// //          for(q <- qs){
// //            BlackHole.consumeAny(quadTree.rangeSearch(q, range))
// //          }
// //        }
// //      }, runCount/pathCount*10, warmUpCount/pathCount*10)
// //
// //      val tSepSet = warmUpAndMeasureTime({
// //        for(qs <- paths){
// //          val neighbors = for(q <- qs) yield {
// //            quadTree.rangeSearch(q, range)
// //          }
// //          BlackHole.consumeAny(neighbors.flatten.toSet)
// //        }
// //      }, runCount/pathCount*10, warmUpCount/pathCount*10)
// //
// //      val tSepSet2 = warmUpAndMeasureTime({
// //        for(qs <- paths){
// //          val s = mutable.Set[Float2]()
// //          qs.foreach{ q =>
// //            s ++= quadTree.rangeSearch(q, range)
// //          }
// //          BlackHole.consumeAny(s)
// //        }
// //      }, runCount/pathCount*10, warmUpCount/pathCount*10)
// //
// //
// //
// //      info(s"\n== Sequence path query test with $pathPoints points ==")
// //      info(s"Total sequence-based time: $tSeq (ms)")
// //      info(s"Total separate time: $tSep (ms)")
// //      info(s"Sequence-based vs separate range query time: ${tSeq/tSep}")
// //      info(s"Sequence-based vs separate unique range query time: ${tSeq/tSepSet}")
// //      info(s"Sequence-based vs separate unique range query time 2: ${tSeq/tSepSet2}")
// //      //assert(similarOrBetterTime(tSeq, tSepSet2, similarityRatio = 1.5))
// //    }
// //  }
// //
// //  it should "have similar sequence knn query performance than with separate queries" in {
// //    val slidingQueryPoints = queryPoints.sliding(20)
// //
// //    val tSeq = warmUpAndMeasureTime({
// //      for(qs <- slidingQueryPoints){
// //        BlackHole.consumeAny(quadTree.seqKnnSearch(qs, queryK))
// //      }
// //    }, runCount, warmUpCount)
// //
// //    val tSep = warmUpAndMeasureTime({
// //      for(qs <- slidingQueryPoints){
// //        for(q <- qs){
// //          BlackHole.consumeAny(quadTree.knnSearch(q, queryK))
// //        }
// //      }
// //    }, runCount, warmUpCount)
// //
// //    val tSepSet = warmUpAndMeasureTime({
// //      for(qs <- slidingQueryPoints){
// //        val neighbors = for(q <- qs) yield {
// //          quadTree.knnSearch(q, queryK)
// //        }
// //        BlackHole.consumeAny(neighbors.flatten.toSet)
// //      }
// //    }, runCount, warmUpCount)
// //
// //    info(s"\nSequence-based vs separate knn query time: ${tSeq/tSep}")
// //    info(s"Sequence-based vs separate unique knn query time: ${tSeq/tSepSet}")
// //    assert(similarTime(tSeq, tSep))
// //  }
//   // TODO: may be broken
//   ignore should "have better removal performance than rebuilding the KdTree" in {
//     val removeCount = points.size/50
//     val removedPoints = points.take(removeCount)
//     val remainingPoints = points.drop(removeCount)
//     val remainingPointsArray = remainingPoints.map{_.toDoubleArray}
//     val totalRemovalCount = insertRunCount*removeCount

//     var quadTree: QuadTree[Float2] = null // scalastyle:ignore null

//     def initBlock(ps: collection.Seq[Float2]): Unit = {
//       quadTree = QuadTree[Float2](bb)
//       ps.foreach{quadTree.add}
//     }

//     val tSingle = warmUpAndMeasureTimeWithInit(
//       initBlock(points),
//       {
//         removedPoints.foreach{e => quadTree.remove(e)}},
//       insertRunCount, warmUpCount)

//     val tSimult = warmUpAndMeasureTimeWithInit(
//       initBlock(points),
//       quadTree.remove(removedPoints),
//       insertRunCount, warmUpCount)

//     val tRebuild = warmUpAndMeasureTime(
//       initBlock(remainingPoints),
//       insertRunCount, warmUpCount)

//     val tRebuildKd = warmUpAndMeasureTime({
//       val kdTree1 = new KDTree[Float2](2, 48)
//       remainingPointsArray.foreach(k => kdTree1.add(k, f2))
//     }, insertRunCount, warmUpCount)

//     info("\n== Removal vs. rebuild time ==")
//     info("Removing " + removeCount + " points (" + removeCount.toDouble/points.size + ")")
//     info("QuadTree (removal): " + tSingle + " (total time), " + tSingle/totalRemovalCount + " (ms/rem)")
//     info("QuadTree (removal simult): " + tSimult + " (total time), " + tSimult/totalRemovalCount + " (ms/rem)")
//     info("QuadTree (rebuild): " + tRebuild + " (total time)")
//     info("KdTree (rebuild): " + tRebuildKd + " (total time)")
//     tSingle should be < tRebuildKd
//   }

// //  it should "have balanced range-until-first-found search performance" in {
// //    val ranges = Seq(0.001f, 0.005f, 0.01f, 0.1f, 0.2f).map{_ * bb.width}
// //    for(r <- ranges){
// //      val t = warmUpAndMeasureTime({
// //        for (q <- queryPoints) {
// //          BlackHole.consumeAny(quadTree.rangeUntilFirstFound(q, r))
// //        }
// //      }, runCount, warmUpCount)
// //
// //      val tRange = warmUpAndMeasureTime({
// //        for (q <- queryPoints) {
// //          BlackHole.consumeAny(quadTree.rangeSearch(q, r).headOption)
// //        }
// //      }, runCount, warmUpCount)
// //
// //      val tNN = warmUpAndMeasureTime({
// //        for (q <- queryPoints) {
// //          BlackHole.consumeAny(quadTree.nearestNeighborSearch(q))
// //        }
// //      }, runCount, warmUpCount)
// //
// //      info(s"\nRangeUntilFirstFound with r = $r")
// //      info(s"Ratio (rangeUntilF/range.headOption: ${t/tRange}")
// //      if(r >= 0.1f*bb.width){
// //        assert(similarOrBetterTime(t, tRange))
// //      }
// //
// //      info(s"Ratio (rangeUntilF/nearestNeighbor: ${t/tNN}")
// //
// //    }
// //
// //
// //  }
// }
