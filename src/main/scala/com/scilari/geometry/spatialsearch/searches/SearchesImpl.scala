package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.{AABB, Float2}

import scala.collection.mutable

/**
  * Concrete implementations with distance functions for searches member variables
  * @tparam E Element type
  */
trait SearchesImpl[E <: Float2] extends TreeSearches.Combined[Float2, E] {
  import SearchesImpl._
  val basicSearches: BasicSearches[Float2, E] = new PointDistances[E]
  val polygonalSearches: PolygonalSearches[E] = new PointDistances[E]
  val seqSearches: BasicSearches[mutable.Buffer[Float2], E] = new SeqDistances[mutable.Buffer[Float2], E]
}

object SearchesImpl{
    class PointDistances[E <: Float2] extends BasicSearches[Float2, E] with PolygonalSearches[E] {
      override type NodeType <: Node with AABB
      override def elemDist(p: Float2, e: E): Float = p.distanceSq(e)
      override def nodeDist(p: Float2, n: NodeType): Float = n.distanceSq(p)
    }

    class SeqDistances[P <: mutable.Buffer[Float2], E <: Float2] extends BasicSearches[P, E] {
      override type NodeType <: Node with AABB

      override def elemDist(ps: P, e: E): Float = {
        var minD = Float.PositiveInfinity
        var i = 0
        val n = ps.size
        while(i < n){
          minD = math.min(minD, e.distanceSq(ps(i)))
          i += 1
        }
        minD
      }

      override def nodeDist(ps: P, node: NodeType): Float = {
        // Optimized by using a sentinel with early exit with distance == 0f
        val lastIndex = ps.size - 1
        val last = ps.last
        ps.asInstanceOf[mutable.IndexedSeq[Float2]](lastIndex) = node.center
        val lastDistance = node.distanceSq(last)
        var minD = lastDistance
        var i = 0
        while(minD > 0f){
          minD = math.min(minD, node.distanceSq(ps(i)))
          i += 1
        }
        ps.asInstanceOf[mutable.IndexedSeq[Float2]](lastIndex) = last
        if(i == ps.size) lastDistance else minD
      }
    }
}
