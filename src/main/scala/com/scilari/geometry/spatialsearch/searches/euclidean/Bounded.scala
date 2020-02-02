package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{AABB, Float2, HasPosition}
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

trait Bounded {
  def bounds: AABB
}

object Bounded{
  protected trait BaseBounded extends DistanceConfig{
    type Q = Float2
    type E <: HasPosition
    override type NodeType <: Bounded
  }

  trait EuclideanBounded extends BaseBounded {
    override def elemDist(q: Q, e: E): Float = Float2.distanceSq(e.position, q)
    override def nodeDist(q: Q, b: NodeType): Float = b.bounds.distanceSq(q)
  }

  trait ManhattanBounded extends BaseBounded {
    override def elemDist(q: Q, e: E): Float = Float2.manhattan(e.position, q)
    override def nodeDist(q: Q, b: NodeType): Float = b.bounds.manhattan(q)
  }
}
