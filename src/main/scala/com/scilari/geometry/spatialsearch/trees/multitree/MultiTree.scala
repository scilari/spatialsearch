package com.scilari.geometry.spatialsearch.trees.multitree

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.Searchable
import com.scilari.geometry.spatialsearch.searches.SearchesImpl
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree

/**
  * Spatial search functionality from multiple trees at once
  * @param trees The trees to search from
  * @tparam E Element type
  */
class MultiTree[E <: Float2] private (trees: Seq[BoundedSearchTree[E]]) extends SearchesImpl[E] with Searchable[E] {

  var root: BaseType = null.asInstanceOf[BaseType] // scalastyle:ignore null

  private[this] val roots = trees.map{tree =>
    tree.root.asInstanceOf[basicSearches.BaseType with polygonalSearches.BaseType with seqSearches.BaseType]
  }

  override def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    import basicSearches._
    search(State(queryPoint, roots), new KnnParameters(k))
  }

  override def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    import basicSearches._
    search(State(queryPoint, roots), new KnnWithCondition(k, condition))
  }

  override def rangeSearch(queryPoint: Float2, r: Float): Seq[E] = {
    import basicSearches._
    search(State(queryPoint, roots), new RangeParameters(r))
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    import polygonalSearches._
    search(State(queryPoint, roots), new PolygonalParameters())
  }

  override def fastPolygonalSearch(queryPoint: Float2): Seq[E] = {
    import polygonalSearches._
    search(State(queryPoint, roots), new PolygonalDynamicMaxRange(3f))
  }

  override def seqKnnSearch(queryPoints: IndexedSeq[Float2], k: Int): Seq[E] = {
    import seqSearches._
    search(State(queryPoints, roots), new KnnParameters(k))
  }

  override def seqRangeSearch(queryPoints: IndexedSeq[Float2], r: Float): Seq[E] = {
    import seqSearches._
    search(State(queryPoints, roots), new RangeParameters(r))
  }

  //override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = super.isEmptyRange(queryPoint, r)

  def isEmpty: Boolean = roots.forall(_.isEmpty)
  def nonEmpty: Boolean = roots.exists(_.nonEmpty)

}

object MultiTree{
  def apply[E <: Float2](trees: Seq[BoundedSearchTree[E]]): Searchable[E] = new MultiTree[E](trees)
}
