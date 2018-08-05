package com.scilari.geometry.performance

import com.csdgn.util.KDTree
import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.TestUtils.Timing.{similarTime, warmUpAndMeasureTime}
import com.scilari.geometry.spatialsearch.trees.rtree.RTree
import org.scalatest.{FlatSpec, Matchers}

class RTreePerformanceTests extends FlatSpec with Matchers with PerformanceBase {
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
