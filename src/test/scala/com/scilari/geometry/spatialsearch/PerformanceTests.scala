package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}
import com.scilari.geometry.spatialsearch.rtree.RTree
import TestUtils.Timing._
import com.scilari.geometry.spatialsearch.plotting.TreePlotter
import org.csdgn.util.KDTree
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class PerformanceTests extends FlatSpec {
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


  val f2 = Float2.random()

  // Data with two clusters and a small number of random points elsewhere
  val points  =
    Seq.fill(pointCount/2)(Float2.random(0.4f*bb.width)) ++
      Seq.fill(pointCount/2)(Float2.random(0.4f*bb.width) + 0.6f*bb.width) ++
      Seq.fill(pointCount/20)(Float2.random(bb.width))

  //val bb = AABB(points)



  val queryPoints: Seq[Float2] = {
    Seq.fill(queryCount){Float2.random(bb.minPoint, bb.maxPoint)}
  }

  val pointsArray = points.map{_.toDoubleArray}
  val queryArray = queryPoints.map{_.toDoubleArray}
  val kdTree = new KDTree[Float2](2)
  pointsArray.foreach(k => kdTree.add(k, Float2.random))
  val quadTree = QuadTree(points)
  val rTree = RTree(points)



  def testInfo: Unit ={
    TreePlotter.plot(quadTree, "QuadTree", elemRadius = bb.width/500)
    TreePlotter.plot(rTree, "rTree", elemRadius = bb.width/500)
    info("== Test info == ")
    info("Point count: " + pointCount)
    info("Knn k: " + queryK)
    info("Range: " + range + " out of total point area of " + bb.width + " x " + bb.height)
    info("Quadtree. depth: " +  quadTree.depth + " nodeCount: " + quadTree.root.nodes.size)
    info("======================")
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

    info("Insert time")
    info("KDTree: " + tKd  + " ms/insert: " + tKd/totalInsertCount)
    info("QuadTree: " + tQd + " ms/insert: " + tQd/totalInsertCount)
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

    info("Knn time")
    info("KDTree: " + tKd + " ms/query: " + tKd/totalQueryCount)
    info("QuadTree: " + tQd + " ms/query: " + tQd/totalQueryCount)
    assert(similarTime(tKd, tQd))
  }

  it should "have similar range query performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      for(q <- queryArray){
        val kdRangeLow = Array(q(0) - range, q(1) - range)
        val kdRangeHigh = Array(q(0) + range, q(1) + range)
        kdTree.getRange(kdRangeLow, kdRangeHigh)
      }
    }, runCount, warmUpCount)

    val tQd = warmUpAndMeasureTime({
      for(q <- queryPoints){
        quadTree.rangeSearch(q, range)
      }
    }, runCount, warmUpCount)

    info("Range time")
    info("KDTree: " + tKd + " ms/query: " + tKd/totalQueryCount)
    info("QuadTree: " + tQd + " ms/query: " + tQd/totalQueryCount)
    assert(similarTime(tKd, tQd))
  }

  it should "have similar performance with polygonal search and range search with similar range" in {
    // polygonal search improves with smaller nodeElementCapacity
    val quadTree = QuadTree(points, Parameters(nodeElementCapacity = 15))


    val maxRanges: Seq[Float] = for(q <- queryPoints) yield quadTree.polygonalSearch(q).map{_.distance(q)}.max
    val minRanges: Seq[Float] = for(q <- queryPoints) yield quadTree.polygonalSearch(q).map{_.distance(q)}.min
    val ratios = maxRanges.zip(minRanges).map{case(max, min) => max/min}
    //println("Avg ratio: " + ratios.sum/ratios.size)
    val meanRange = maxRanges.sum/maxRanges.size


    val tPol = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.polygonalSearch(q)
      }
    }, runCount, warmUpCount)

    val tPolMax = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.polygonalMaxRangeSearch(q, bb.width/20)
      }
    }, runCount, warmUpCount)

    val tPolDyn = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = quadTree.polygonalDynamicMaxRangeSearch(q, 3f)
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

    info("Polygonal vs. knn time")
    info("QuadTree (polygonal): " + tPol + " ms/query: " + tPol/totalQueryCount)
    info("QuadTree (poly max r): " + tPolMax + " ms/query: " + tPolMax/totalQueryCount)
    info("QuadTree (poly dyn max r): " + tPolDyn + " ms/query: " + tPolDyn/totalQueryCount)
    info("QuadTree (range): " + tR + " ms/query: " + tR/totalQueryCount)
    info("QuadTree (range: " + meanRange +  "): " + tMeanRange + " ms/query: " + tMeanRange/totalQueryCount)
  }

  it should "should have better removal performance than rebuilding the KdTree" in {
    //info("Leaf count: " + quadTree.root.leaves.size)
    val removeCount = points.size/50
    val removedPoints = points.take(removeCount)
    val remainingPoints = points.drop(removeCount)
    val remainingPointsArray = remainingPoints.map{_.toDoubleArray}
    val totalRemovalCount = insertRunCount*removeCount
    info("Removing " + removeCount + " points (" + removeCount.toDouble/points.size + ")")

    var quadTree: QuadTree[Float2] = null

    def initBlock() = {
      quadTree = QuadTree[Float2](bb)
      points.foreach{quadTree.add}
    }

    val tSingle = warmUpAndMeasureTimeWithInit(
      initBlock,
      {
        removedPoints.foreach{e => quadTree.remove(e, e)}},
      insertRunCount, warmUpCount)

    val tSimult = warmUpAndMeasureTimeWithInit(
      initBlock,
      quadTree.remove(removedPoints),
      insertRunCount, warmUpCount)

    val tRebuild = warmUpAndMeasureTime(
      initBlock,
      insertRunCount, warmUpCount)

    val tRebuildKd = warmUpAndMeasureTime({
      val kdTree1 = new KDTree[Float2](2, 48)
      pointsArray.foreach(k => kdTree1.add(k, f2))
    }, insertRunCount, warmUpCount)

    info("Removal vs. rebuild time")
    info("QuadTree (removal): " + tSingle + " ms/rem: " + tSingle/totalRemovalCount)
    info("QuadTree (removal simult): " + tSimult + " ms/rem: " + tSimult/totalRemovalCount)
    info("QuadTree (rebuild): " + tRebuild)
    info("KdTree (rebuild): " + tRebuildKd)
    tSingle should be < tRebuildKd
  }


  "RTree" should "have better insertion performance than KDTree" in {
    val tKd = warmUpAndMeasureTime({
      val kdTree = new KDTree[Float2](2, 48)
      pointsArray.foreach(k => kdTree.add(k, f2))
    }, insertRunCount, warmUpCount)

    val tQd = warmUpAndMeasureTime({
      val rTree = RTree[Float2]()
      points.foreach(rTree.add)
    }, insertRunCount, warmUpCount)

    info("Insert time")
    info("KDTree: " + tKd)
    info("RTree: " + tQd)
  }


  it should "have similar knn query performance to KDTree" in {
    val tKd = warmUpAndMeasureTime({
      for(q <- queryArray){
        val neighbors = kdTree.getNearestNeighbors(q, queryK)
      }
    }, runCount, warmUpCount)

    val tQd = warmUpAndMeasureTime({
      for(q <- queryPoints){
        val neighbors = rTree.knnSearch(q, queryK)
      }
    }, runCount, warmUpCount)

    info("Knn time")
    info("KDTree: " + tKd)
    info("RTree: " + tQd)
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
        val neighbors = rTree.rangeSearch(q, range)
      }
    }, runCount, warmUpCount)

    info("Range time")
    info("KDTree: " + tKd)
    info("RTree: " + tQd)
    assert(similarTime(tKd, tQd))
  }


}
