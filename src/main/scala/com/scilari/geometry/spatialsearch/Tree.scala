package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.MetricObject

/**
  * Tree with metric children
  * @tparam P Query point type
  * @tparam E Element type
  */
trait Tree[P, E] extends MetricObject[P] with Traversable[E]{
  def children: Seq[MetricObject[P]]
  def getElements: Seq[E]
  override def foreach[U](f: E => U): Unit = getElements.foreach(f)
}

object Tree{

  /**
    * Node with metric children nodes
    * @tparam P Query point type
    * @tparam E Element type
    * @tparam N Node type
    */
  trait Node[P, E, N <: Tree[P, E]] extends Tree[P, E]{
    override def children: Seq[N]
    override def getElements: Seq[E] = children.flatMap(_.getElements)
  }

  /**
    * Leaf node with metric elements
    * @tparam P Type of query point
    * @tparam E Element type
    */
  trait Leaf[P, E <: MetricObject[P]] extends Tree[P, E]{
    override def children: Seq[E]
    override def getElements: Seq[E] = children
  }

}





