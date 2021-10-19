package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.IncrementalSearch.NonFilteringIncrementalSearch
import com.scilari.geometry.spatialsearch.core.SearchState.DefaultInitialState
import com.scilari.geometry.spatialsearch.core.{IncrementalSearch, Rooted, SearchConfig, SearchState}
import com.scilari.geometry.spatialsearch.core.SearchConfig.{InitialState, SearchParameters}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Node
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.queues.{FloatMaxHeapK, FloatMinHeap}

import scala.collection.mutable.ArrayBuffer

object KnnSearches {
  trait BaseKnn[E <: Position] extends SearchConfig[Float2, E] with Rooted[E] {
    val k: Int
    var root: Node[E]
    
    def initialState(q: Float2): SearchState[Float2, E] = {
      new SearchState[Float2, E](
        q,
        FloatMinHeap[Node[E]](0, root, 4),
        new FloatMaxHeapK[E](k),
        new ArrayBuffer[E](k)
      )
    }
    
    inline def maxElemDist(s: State) = if (s.elements.isEmpty) Float.MaxValue else s.elements.maxKey
    inline def minNodeDist(s: State) = if (s.nodes.isEmpty) Float.MaxValue else s.nodes.minKey
    inline def elemCloserThanNode(s: State) = minNodeDist(s) >= maxElemDist(s)
    
    override def endCondition(s: State): Boolean = {
      val done = s.nodes.isEmpty || (elemCloserThanNode(s) && s.elements.size == k)
      if (done) {
        s.elements.peekValuesToBuffer(s.foundElements)
      }
      done
    }
  }
  
  trait Knn[E <: Position] extends BaseKnn[E] with NonFilteringIncrementalSearch[Float2, E] 
  
  trait KnnWithFilter[E <: Position](val k: Int, filter: E => Boolean) extends BaseKnn[E] 
    with IncrementalSearch[Float2, E] {
    
    inline override def filterElements(e: E, s: State): Boolean = filter(e)
  }

  trait KnnWithinRadius[E <: Position](val k: Int, r: Float) extends BaseKnn[E] with IncrementalSearch[Float2, E] {
    val rSq = r * r
    override def filterElements(e: E, s: State): Boolean = e.position.distanceSq(s.queryPoint) <= rSq
    override def filterNodes(n: QuadTree.Node[E], s: State): Boolean = n.bounds.distanceSq(s.queryPoint) <= rSq
  }

}



