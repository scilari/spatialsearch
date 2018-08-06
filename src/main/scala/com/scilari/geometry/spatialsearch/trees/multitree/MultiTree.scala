package com.scilari.geometry.spatialsearch.trees.multitree

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.Searchable
import com.scilari.geometry.spatialsearch.searches.{SearchesImpl, TreeSearches}
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree

/**
  * Spatial search functionality from multiple trees at once
  * @param trees The trees to search from
  * @tparam E Element type
  */
class MultiTree[E <: Float2] private (trees: Seq[TreeSearches.Base[Float2, E]]) extends SearchesImpl[E] with Searchable[E] {

  var root: NodeType = null.asInstanceOf[NodeType] // scalastyle:ignore null

  private[this] val roots = trees.map{tree =>
    tree.root.asInstanceOf[basicSearches.NodeType with polygonalSearches.NodeType with seqSearches.NodeType]
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

  def isEmpty: Boolean = !nonEmpty
  def nonEmpty: Boolean = roots.exists(_.nonEmpty)

  def nonEmptyIfNotEmptied: Boolean = roots.exists(_.nonEmptyIfNotEmptied)

  def elements: Seq[E] = roots.flatMap(_.elements)

}

object MultiTree{
  def apply[E <: Float2](trees: Seq[BoundedSearchTree[E]]): MultiTree[E] = new MultiTree[E](trees)
}
