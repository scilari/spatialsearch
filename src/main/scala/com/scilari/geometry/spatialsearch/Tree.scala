package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.MetricObject

/**
  * Tree with metric children
  * @tparam P Type of query point
  */
trait Tree[P] extends MetricObject[P]{
  def children: Seq[MetricObject[P]]
  def isEmpty: Boolean = children.isEmpty
  def nonEmpty: Boolean = children.nonEmpty
}

object Tree{

  /**
    * Node with metric children nodes
    * @tparam P Type of query point
    * @tparam N Type of children nodes
    */
  trait Node[P, N <: Tree[P]] extends Tree[P]{
    override def children: Seq[N]
  }

  /**
    * Leaf node with metric elements
    * @tparam P Type of query point
    * @tparam E Element type
    */
  trait Leaf[P, E <: MetricObject[P]] extends Tree[P]{
    override def children: Seq[E]
  }
}





