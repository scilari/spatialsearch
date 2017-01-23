package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.MetricObject

/**
  * Created by iv on 1/17/2017.
  * TODO: possibly implement and measure forEachChildrenWithFilter for performance
  */
trait Tree[P] extends MetricObject[P]{
  def children: Seq[MetricObject[P]]
  def isEmpty: Boolean = children.isEmpty
  def nonEmpty: Boolean = children.nonEmpty
}

object Tree{
  trait Node[P, N <: Tree[P]] extends Tree[P]{
    override def children: Seq[N]
  }

  trait Leaf[P, E <: MetricObject[P]] extends Tree[P]{
    override def children: Seq[E]
  }
}





