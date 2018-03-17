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

  private[this] val roots = trees.map{tree => tree.root.asInstanceOf[BaseType]}

  override def elemDist(p: Float2, e: E): Float = p.distanceSq(e)

  override def nodeDist(p: Float2, n: BaseType): Float = n.distanceSq(p)

  override def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    search(State(queryPoint, roots), new KnnParameters(k))
  }

  override def rangeSearch(queryPoint: Float2, r: Float): Seq[E] = {
    search(State(queryPoint, roots), new RangeParameters(r))
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    search(State(queryPoint, roots), new PolygonalParameters())
  }

  override def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    search(State(queryPoint, roots), new KnnWithCondition(k, condition))
  }

  override def fastPolygonalSearch(queryPoint: Float2): Seq[E] = {
    search(State(queryPoint, roots.asInstanceOf[Seq[BaseType]]), new PolygonalDynamicMaxRange(3f))
  }

  override def isEmpty: Boolean = roots.forall(_.isEmpty)
  override def nonEmpty: Boolean = roots.exists(_.nonEmpty)

}
