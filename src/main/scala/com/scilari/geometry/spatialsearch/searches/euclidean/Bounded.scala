package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

trait Bounded {
  def bounds: AABB
}

object Bounded{
  protected trait BaseBounded extends DistanceConfig{
    type Q = Float2
    type E <: Float2
    override type NodeType <: Bounded
  }

  trait EuclideanBounded extends BaseBounded {
    override def elemDist(q: Q, e: E): Float = Float2.distanceSq(e, q)
    override def nodeDist(q: Q, b: NodeType): Float = b.bounds.distanceSq(q)
  }

  trait ManhattanBounded extends BaseBounded {
    override def elemDist(q: Q, e: E): Float = Float2.manhattan(q, e)
    override def nodeDist(q: Q, b: NodeType): Float = b.bounds.manhattan(q)
  }
}
