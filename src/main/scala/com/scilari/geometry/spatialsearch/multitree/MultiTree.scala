package com.scilari.geometry.spatialsearch.multitree

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch._

/**
  * Spatial search functionality from multiple trees at once
  * @param trees The trees to search from
  * @tparam E Element type
  */
class MultiTree[E <: Float2](trees: Seq[BoundedSearchTree[E]]) extends Searchable[E]
  with Searches[Float2, E] with PolygonalSearches[Float2, E]{

  private[this] val roots =  trees.map{_.root.asInstanceOf[Tree[Float2, E]#BaseType]}

  override def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    val knn = new Knn(k)
    // TODO: fix all these casts
    knn.search(knn.State(queryPoint, roots.asInstanceOf[Seq[knn.BaseType]]))
  }

  override def rangeSearch(queryPoint: Float2, r: Float): Seq[E] = {
    val range = new Range(r)
    range.search(range.State(queryPoint, roots.asInstanceOf[Seq[range.BaseType]]))
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    val poly = new Polygonal()
    poly.search(poly.State(queryPoint, roots.asInstanceOf[Seq[poly.BaseType]]))
  }

  override def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    val knnCond = new KnnWithCondition(k, condition)
    knnCond.search(knnCond.State(queryPoint, roots.asInstanceOf[Seq[knnCond.BaseType]]))
  }

  override def fastPolygonalSearch(queryPoint: Float2): Seq[E] = {
    val poly = new PolygonalDynamicMaxRange(3f)
    poly.search(poly.State(queryPoint, roots.asInstanceOf[Seq[poly.BaseType]]))
  }

  override def isEmpty: Boolean = roots.forall(_.isEmpty)
  override def nonEmpty: Boolean = roots.exists(_.nonEmpty)
}
