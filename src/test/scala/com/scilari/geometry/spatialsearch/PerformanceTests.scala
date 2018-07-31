package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.quadtree.{Parameters, QuadTree}
import com.scilari.geometry.spatialsearch.trees.rtree.RTree
import TestUtils.Timing._
import com.scilari.geometry.spatialsearch.plotting.TreePlotter
import org.csdgn.util.KDTree
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable


class PerformanceTests extends FlatSpec with Matchers {
  import QuadTree._
  val plotting = true

  val similarityRatio = 2.0
  val runCount = 200
  val insertRunCount = 10*runCount
  val warmUpCount = 2
  val pointCount = 10000
  val queryCount = 1000
  val bb = AABB(1000f)
  val range = 0.25f*bb.width
  val queryK = 100 //pointCount/10


  val totalQueryCount = runCount * queryCount
  val totalInsertCount = insertRunCount * pointCount


  val f2 = Float2.random

  // Data with two clusters and a small number of random points elsewhere
  val points  =
    Seq.fill(pointCount*9/20)(Float2.random(0.2f*bb.width) + 0.1f*bb.width) ++
      Seq.fill(pointCount*9/20)(Float2.random(0.5f*bb.width) + 0.45f*bb.width) ++
      Seq.fill(pointCount*2/20)(bb.randomEnclosedPoint)



  val queryPoints: Seq[Float2] = {
    Seq.fill(queryCount){Float2.random(bb.minPoint, bb.maxPoint)}
  }

  val pointsArray = points.map{_.toDoubleArray}
  val queryArray = queryPoints.map{_.toDoubleArray}
  val kdTree = new KDTree[Float2](2)
  pointsArray.foreach(k => kdTree.add(k, Float2.random))
  val quadTree: QuadTree[Float2] = QuadTree(points)
  val rTree: RTree[Float2] = RTree(points)



  def testInfo: Unit ={
    if(plotting){
      TreePlotter.plot(quadTree, "QuadTree", elemRadius = bb.width/500)
      TreePlotter.plot(rTree, "rTree", elemRadius = bb.width/500)
    }

    info("== Test info == ")
    info("Point count: " + pointCount)
    info("Run count: " + runCount)
    info("Query count (per run): " + queryCount)
    info("Insert run count: " + insertRunCount)
    info("Knn k: " + queryK)
    info("Range: " + range + " out of total point area of " + bb.width + " x " + bb.height)
    info("Quadtree. depth: " +  quadTree.depth + " nodeCount: " + quadTree.root.nodes.size)
    info("RTree. depth: " +  rTree.depth + " nodeCount: " + rTree.root.nodes.size)
    info("================")
  }


  "QuadTree" should "have similar insertion performance to KDTree" in {
    testInfo
    val tKd = warmUpAndMeasureTime({
      val kdTree = new KDTree[Float2](2, 48)
      pointsArray.foreach(k => kdTree.add(k, f2))
    }, insertRunCount, warmUpCount)

    val tQd = warmUpAndMeasureTime({
      val quadTree = QuadTree[Float2](bb)
      points.foreach(quadTree.add)
    }, insertRunCount, warmUpCount)

    info("\n== Insert time == ")
    info("KDTree: " + tKd/totalInsertCount  + " (ms/insert)" )
    info("QuadTree: " + tQd/totalInsertCount  + " (ms/insert)" )
    info("Ratio (Quad/KD): " + tQd/tKd )
    assert(similarTime(tKd, tQd))
  }


  it should "have similar knn query performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      for(q <- queryArray){
        val neighbors = kdTree.getNearestNeighbors(q, queryK)
      }
    }, runCount, warmUpCount)

    val tQd = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.knnSearch(q, queryK)
      }
    }, runCount, warmUpCount)

    info("\n== Knn query time == ")
    info("KDTree: " + tKd/totalQueryCount + " (ms/query)")
    info("QuadTree: " + tQd/totalQueryCount + " (ms/query)")
    info("Ratio (Quad/KD): " + tQd/tKd )
    assert(similarTime(tKd, tQd))
  }

  it should "have similar range query performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      for(q <- queryArray){
        val kdRangeLow = Array(q(0) - range, q(1) - range)
        val kdRangeHigh = Array(q(0) + range, q(1) + range)
        val neighbors = kdTree.getRange(kdRangeLow, kdRangeHigh)
      }
    }, runCount, warmUpCount)

    val tQd = warmUpAndMeasureTime({
      for(q <- queryPoints){
        quadTree.rangeSearch(q, range)
      }
    }, runCount, warmUpCount)

    info("\n== Range query time ==")
    info("KDTree: " + tKd/totalQueryCount + " (ms/query)")
    info("QuadTree: " + tQd/totalQueryCount + " (ms/query)")
    info("Ratio (Quad/KD): " + tQd/tKd)
    assert(similarTime(tKd, tQd))
  }

  it should "have similar performance with polygonal search and range search with similar range" in {
    // polygonal search improves with smaller nodeElementCapacity
    val quadTree = QuadTree(points, Parameters(nodeElementCapacity = 15))

    val maxRanges: Seq[Float] = for(q <- queryPoints) yield quadTree.polygonalSearch(q).map{_.distance(q)}.max
    val minRanges: Seq[Float] = for(q <- queryPoints) yield quadTree.polygonalSearch(q).map{_.distance(q)}.min
    val meanRange = maxRanges.sum/maxRanges.size

    val tPol = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.polygonalSearch(q)
      }
    }, runCount, warmUpCount)


    val tFastPol = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.fastPolygonalSearch(q)
      }
    }, runCount, warmUpCount)

    val tR = warmUpAndMeasureTime({
      for((q, r) <- queryPoints zip maxRanges){
        val neighbors = quadTree.rangeSearch(q, r)
      }
    }, runCount, warmUpCount)

    val tMeanRange = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.rangeSearch(q, meanRange)
      }
    }, runCount, warmUpCount)

    assert(tPol > tFastPol)

    info("\n== Polygonal vs. range time ==")
    info("QuadTree (polygonal): " + tPol/totalQueryCount + " (ms/query)")
    info("QuadTree (fast poly): " + tFastPol/totalQueryCount + " (ms/query)")
    info("QuadTree (range): " + tR/totalQueryCount + " (ms/query)")
    info("QuadTree (range: " + meanRange +  "): " + tMeanRange/totalQueryCount + " (ms/query)")
  }

  "QuadTree" should "have similar sequence range query performance than with separate queries" in {
    val slidingQueryPoints = queryPoints.sliding(20)

    val tSeq = warmUpAndMeasureTime({
      for(qs <- slidingQueryPoints){
        BlackHole.consumeAny(quadTree.seqRangeSearch(qs, range))
      }
    }, runCount, warmUpCount)

    val tSep = warmUpAndMeasureTime({
      for(qs <- slidingQueryPoints){
        for(q <- qs){
          BlackHole.consumeAny(quadTree.rangeSearch(q, range))
        }
      }
    }, runCount, warmUpCount)

    val tSepSet = warmUpAndMeasureTime({
      for(qs <- slidingQueryPoints){
        val neighbors = for(q <- qs) yield {
          quadTree.rangeSearch(q, range)
        }
        BlackHole.consumeAny(neighbors.flatten.toSet)
      }
    }, runCount, warmUpCount)

    info(s"\nSequence-based vs separate range query time: ${tSeq/tSep}")
    info(s"Sequence-based vs separate unique range query time: ${tSeq/tSepSet}")
    assert(similarTime(tSeq, tSep))
  }

  "QuadTree" should "have similar path sequence range query performance than with separate queries" in {
    val range = 0.1f*bb.width
    val pathCount = 100
    for(pathPoints <- Seq(10, 50, 200)){
      val paths = (0 until pathCount ).map { _ =>
        Float2.linSpace(bb.randomEnclosedPoint, bb.randomEnclosedPoint, pathPoints)
      }

      val tSeq = warmUpAndMeasureTime({
        for(qs <- paths){
          BlackHole.consumeAny(quadTree.seqRangeSearch(qs, range))
        }
      }, runCount/pathCount*10, warmUpCount/pathCount*10)

      val tSep = warmUpAndMeasureTime({
        for(qs <- paths){
          for(q <- qs){
            BlackHole.consumeAny(quadTree.rangeSearch(q, range))
          }
        }
      }, runCount/pathCount*10, warmUpCount/pathCount*10)

      val tSepSet = warmUpAndMeasureTime({
        for(qs <- paths){
          val neighbors = for(q <- qs) yield {
            quadTree.rangeSearch(q, range)
          }
          BlackHole.consumeAny(neighbors.flatten.toSet)
        }
      }, runCount/pathCount*10, warmUpCount/pathCount*10)

      val tSepSet2 = warmUpAndMeasureTime({
        for(qs <- paths){
          val s = mutable.Set[Float2]()
          qs.foreach{ q =>
            s ++= quadTree.rangeSearch(q, range)
          }
          BlackHole.consumeAny(s)
        }
      }, runCount/pathCount*10, warmUpCount/pathCount*10)



      info(s"\n== Sequence path query test with $pathPoints points ==")
      info(s"Sequence-based vs separate range query time: ${tSeq/tSep}")
      info(s"Sequence-based vs separate unique range query time: ${tSeq/tSepSet}")
      info(s"Sequence-based vs separate unique range query time 2: ${tSeq/tSepSet2}")
      assert(similarOrBetterTime(tSeq, tSepSet2, similarityRatio = 1.5))
    }
  }

  it should "have similar sequence knn query performance than with separate queries" in {
    val slidingQueryPoints = queryPoints.sliding(20)

    val tSeq = warmUpAndMeasureTime({
      for(qs <- slidingQueryPoints){
        BlackHole.consumeAny(quadTree.seqKnnSearch(qs, queryK))
      }
    }, runCount, warmUpCount)

    val tSep = warmUpAndMeasureTime({
      for(qs <- slidingQueryPoints){
        for(q <- qs){
          BlackHole.consumeAny(quadTree.knnSearch(q, queryK))
        }
      }
    }, runCount, warmUpCount)

    val tSepSet = warmUpAndMeasureTime({
      for(qs <- slidingQueryPoints){
        val neighbors = for(q <- qs) yield {
          quadTree.knnSearch(q, queryK)
        }
        BlackHole.consumeAny(neighbors.flatten.toSet)
      }
    }, runCount, warmUpCount)

    info(s"\nSequence-based vs separate knn query time: ${tSeq/tSep}")
    info(s"Sequence-based vs separate unique knn query time: ${tSeq/tSepSet}")
    assert(similarTime(tSeq, tSep))
  }

  "QuadTree" should "have better removal performance than rebuilding the KdTree" in {
    val removeCount = points.size/50
    val removedPoints = points.take(removeCount)
    val remainingPoints = points.drop(removeCount)
    val remainingPointsArray = remainingPoints.map{_.toDoubleArray}
    val totalRemovalCount = insertRunCount*removeCount

    var quadTree: QuadTree[Float2] = null // scalastyle:ignore null

    def initBlock(ps: Seq[Float2]): Unit = {
      quadTree = QuadTree[Float2](bb)
      ps.foreach{quadTree.add}
    }

    val tSingle = warmUpAndMeasureTimeWithInit(
      initBlock(points),
      {
        removedPoints.foreach{e => quadTree.remove(e)}},
      insertRunCount, warmUpCount)

    val tSimult = warmUpAndMeasureTimeWithInit(
      initBlock(points),
      quadTree.remove(removedPoints),
      insertRunCount, warmUpCount)

    val tRebuild = warmUpAndMeasureTime(
      initBlock(remainingPoints),
      insertRunCount, warmUpCount)

    val tRebuildKd = warmUpAndMeasureTime({
      val kdTree1 = new KDTree[Float2](2, 48)
      remainingPointsArray.foreach(k => kdTree1.add(k, f2))
    }, insertRunCount, warmUpCount)

    info("\n== Removal vs. rebuild time ==")
    info("Removing " + removeCount + " points (" + removeCount.toDouble/points.size + ")")
    info("QuadTree (removal): " + tSingle + " (total time), " + tSingle/totalRemovalCount + " (ms/rem)")
    info("QuadTree (removal simult): " + tSimult + " (total time), " + tSimult/totalRemovalCount + " (ms/rem)")
    info("QuadTree (rebuild): " + tRebuild + " (total time)")
    info("KdTree (rebuild): " + tRebuildKd + " (total time)")
    tSingle should be < tRebuildKd
  }


  "RTree" should "have even somewhat comparable insertion performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      val kdTree = new KDTree[Float2](2, 48)
      pointsArray.foreach(k => kdTree.add(k, f2))
    }, insertRunCount, warmUpCount)

    val tRt = warmUpAndMeasureTime({
      val rTree = RTree[Float2]()
      points.foreach(rTree.add)
    }, insertRunCount, warmUpCount)

    info("\n== Insert time == ")
    info("KDTree: " + tKd/totalInsertCount  + " (ms/insert)" )
    info("RTree: " + tRt/totalInsertCount  + " (ms/insert)" )
    info("Ratio (Rtree/KD): " + tRt/tKd)
    assert(similarTime(tKd, tRt, similarityRatio = 20))
  }


  it should "have similar knn query performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      for(q <- queryArray){
        val neighbors = kdTree.getNearestNeighbors(q, queryK)
      }
    }, runCount, warmUpCount)

    val tRt = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = rTree.knnSearch(q, queryK)
      }
    }, runCount, warmUpCount)

    info("\n== Knn query time ==")
    info("KDTree: " + tKd/totalQueryCount + " (ms/query)")
    info("RTree: " + tRt/totalQueryCount + " (ms/query)")
    info("Ratio (Rtree/KD): " + tRt/tKd)
    assert(similarTime(tKd, tRt))

  }

  it should "have similar range query performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      for(q <- queryArray){
        val kdRangeLow = Array(q(0) - range, q(1) - range)
        val kdRangeHigh = Array(q(0) + range, q(1) + range)
        val neighbors = kdTree.getRange(kdRangeLow, kdRangeHigh)
      }
    }, runCount, warmUpCount)

    val tRt = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = rTree.rangeSearch(q, range)
      }
    }, runCount, warmUpCount)

    info("\n== Range query time ==")
    info("KDTree: " + tKd/totalQueryCount + " (ms/query)")
    info("RTree: " + tRt/totalQueryCount + " (ms/query)")
    info("Ratio (Rtree/KD): " + tRt/tKd)
    assert(similarTime(tKd, tRt))
  }


}
