package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.IncrementalSearch
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig
import com.scilari.geometry.spatialsearch.core.SearchState.DefaultInitialState
import com.scilari.geometry.spatialsearch.core.Rooted
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

/**
 * Searches that use a sequence of query points and use the minimum distance between the tree element and this sequence.
 * This is convenient for finding the close points to a paths, defined as a sequence, for example. Also, this might
 * give some performance improvement over doing separate searches for the querypoints and finding the union of the
 * results (TODO: write benchmark to verify this).
 *
 * @tparam E ElemType
 */
trait SeqSearches[E <: Position] extends Rooted[E]{
  import SeqSearches._
  
  def seqKnnSearch(queryPoints: Q, k: Int): collection.Seq[E] = SeqKnn[E](root, k).search(queryPoints)
  
  def seqRangeSearch(queryPoints: Q, r: Float): collection.Seq[E] = SeqRange[E](root, r).search(queryPoints)
}

object SeqSearches {
  type Q = collection.Seq[Float2]
  
  // TODO: optimize
  def elemMinDist[E <: Position](qs: Q, e: E): Float = qs.map{ _.distanceSq(e.position) }.min
  def nodeMinDist[E <: Position](qs: Q, n: QuadTree.Node[E]): Float = qs.map{ q => n.bounds.distanceSq(q) }.min
  
  trait SeqDistanceConfig[E <: Position] extends DistanceConfig[Q, E] {
    override def elemDist(qs: Q, e: E): Float = elemMinDist(qs, e)
    override def nodeDist(qs: Q, n: QuadTree.Node[E]): Float = nodeMinDist(qs, n)
  }
  
  final class SeqKnn[E <: Position](var root: QuadTree.Node[E], val k: Int) extends SeqDistanceConfig[E]
    with IncrementalSearch[Q, E] with DefaultInitialState[Q, E] with Rooted[E] {

    override def endCondition(s: State): Boolean = s.foundElements.length >= k
    override val foundElemSizeHint: Int = k
  }
  
  final class SeqRange[E <: Position](var root: QuadTree.Node[E], r: Float) extends SeqDistanceConfig[E]
  with IncrementalSearch[Q, E] with DefaultInitialState[Q, E] with Rooted[E] {
    val rSq = r*r

    override def filterElements(e: E, s: State): Boolean = elemMinDist(s.queryPoint, e) <= rSq

    override def filterNodes(n: QuadTree.Node[E], s: State): Boolean = nodeMinDist(s.queryPoint, n) <= rSq
  }
  
}
