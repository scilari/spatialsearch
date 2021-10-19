package com.scilari.geometry.performance

import com.csdgn.util.KDTree
import jk.tree.KDTree.Euclidean

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree


// TODO: make this object
object PerformanceBase {
  val plotting = true

  val runCount: Int = 500
  val insertRunCount: Int = 10*runCount
  val warmUpCount: Int = 2
  val pointCount: Int = 10000
  val queryCount: Int = 1000
  val bb: AABB = AABB.positiveSquare(1000f)
  val range: Float = 0.1f*bb.width
  val queryK: Int = 100 // math.min(pointCount/10, 100)

  val totalQueryCount: Int = runCount * queryCount
  val totalInsertCount: Int = insertRunCount * pointCount

  val f2: Float2 = Float2.random
  
  def createPoints(pointCount: Int): collection.Seq[Float2] = (
    collection.Seq.fill(pointCount*49/100)(Float2.random(0.2f*bb.width) + 0.1f*bb.width) ++
      collection.Seq.fill(pointCount*49/100)(Float2.random(0.5f*bb.width) + 0.45f*bb.width) ++
      collection.Seq.fill(pointCount*2/100)(bb.randomEnclosedPoint))

  // Data with two clusters and a small number of random points elsewhere
  val points = createPoints(pointCount)
  
  def createTree(n: Int) = QuadTree(createPoints(n))



  val queryPoints: Array[Float2] = {
    Array.fill(queryCount){Float2.random(bb.minPoint, bb.maxPoint)}
  }

  val pointsArray: collection.Seq[Array[Double]] = points.map{_.position.toDoubleArray}
  val queryArray: collection.Seq[Array[Double]] = queryPoints.map{_.toDoubleArray}
  
  val kdTree = new KDTree[Float2](2)
  pointsArray.foreach(k => kdTree.add(k, Float2.random))

  val kdTree2 = new Euclidean[Float2](2)
  pointsArray.foreach(k => kdTree2.addPoint(k, Float2.random))
  
  val kdRangeLows: collection.Seq[Array[Double]] = queryArray.map{q => Array(q(0) - range, q(1) - range)}
  val kdRangeHighs: collection.Seq[Array[Double]] = queryArray.map{q =>  Array(q(0) + range, q(1) + range)}

  val quadTree: QuadTree[Float2] = QuadTree(points)

}
