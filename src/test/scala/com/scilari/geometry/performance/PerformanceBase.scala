package com.scilari.geometry.performance

import com.csdgn.util.KDTree
import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.trees.rtree.RTree

import scala.collection.mutable

trait PerformanceBase {
  val plotting = true

  val runCount: Int = 200
  val insertRunCount: Int = 10*runCount
  val warmUpCount: Int = 2
  val pointCount: Int = 10000
  val queryCount: Int = 1000
  val bb: AABB = AABB(1000f)
  val range: Float = 0.25f*bb.width
  val queryK: Int = 100 //pointCount/10


  val totalQueryCount: Int = runCount * queryCount
  val totalInsertCount: Int = insertRunCount * pointCount


  val f2: Float2 = Float2.random

  // Data with two clusters and a small number of random points elsewhere
  val points: Seq[Float2]  =
    Seq.fill(pointCount*9/20)(Float2.random(0.2f*bb.width) + 0.1f*bb.width) ++
      Seq.fill(pointCount*9/20)(Float2.random(0.5f*bb.width) + 0.45f*bb.width) ++
      Seq.fill(pointCount*2/20)(bb.randomEnclosedPoint)


  val queryPoints: mutable.Buffer[Float2] = {
    mutable.Buffer.fill(queryCount){Float2.random(bb.minPoint, bb.maxPoint)}
  }

  val pointsArray: Seq[Array[Double]] = points.map{_.toDoubleArray}
  val queryArray: Seq[Array[Double]] = queryPoints.map{_.toDoubleArray}
  val kdTree = new KDTree[Float2](2)
  pointsArray.foreach(k => kdTree.add(k, Float2.random))
  val quadTree: QuadTree[Float2] = QuadTree(points)
  val rTree: RTree[Float2] = RTree(points)

}
