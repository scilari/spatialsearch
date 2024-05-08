package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.{Position, Float2}
import com.scilari.geometry.spatialsearch.core.SearchConfig.{
  // DistanceConfig,
  InitialState,
  SearchParameters
}
import com.scilari.geometry.spatialsearch.quadtree.Tree.Node
import com.scilari.geometry.spatialsearch.core.State
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.models.AABB
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

/** Three orthogonal traits to define searches.
  *   - DistanceConfig to switch e.g. between Euclidean and Manhattan distance
  *   - SearchParameters to define the search control flow
  *   - InitialState to initialize with root or multiple nodes, for example
  */
trait SearchConfig[E <: Position](using dc: DistanceConfig)
// extends DistanceConfig
    extends SearchParameters[E]
    with InitialState[E] {
  export dc.*
}

object SearchConfig {

  trait DefaultFiltering[E <: Position] extends SearchConfig[E] {
    def addFoundElement(e: E, state: State[E]): Unit =
      if (filterElements(e, state)) state.foundElements += e
    def enqueueElement(e: E, state: State[E]): Unit =
      if (filterElements(e, state)) state.elements.enqueue(elemDist(state.queryPoint, e), e)
    def enqueueNode(n: Node[E], state: State[E]): Unit =
      if (filterNodes(n, state)) state.nodes.enqueue(nodeDist(state.queryPoint, n.bounds), n)
    def collectFoundOrDone(s: State[E]): Boolean = {
      while (s.elemCloserThanNode && !defaultEndCondition(s) && !endCondition(s)) {
        val candidate = s.elements.dequeueValue()
        addFoundElement(candidate, s)
      }
      defaultEndCondition(s) || endCondition(s)
    }
  }

  trait NonFiltering[E <: Position] extends SearchConfig[E] {
    def addFoundElement(candidate: E, state: State[E]): Unit =
      state.foundElements += candidate

    def enqueueElement(e: E, state: State[E]): Unit = {
      state.elements.enqueue(elemDist(state.queryPoint, e), e)
    }

    def enqueueNode(n: Node[E], state: State[E]): Unit = {
      state.nodes.enqueue(nodeDist(state.queryPoint, n.bounds), n)
    }
  }

  trait DistanceConfig {
    def elemDist(q: Float2, e: Position): Float
    def nodeDist(q: Float2, n: AABB): Float
  }

  trait SearchParameters[E <: Position] {
    def endCondition(s: State[E]): Boolean = false
    def filterElements(e: E, s: State[E]): Boolean = true
    def filterNodes(n: Node[E], s: State[E]): Boolean = true
    def addFoundElement(e: E, state: State[E]): Unit
    def enqueueElement(e: E, state: State[E]): Unit
    def enqueueNode(n: Node[E], state: State[E]): Unit
    def collectFoundOrDone(s: State[E]): Boolean
    def defaultEndCondition(s: State[E]) = s.nodes.isEmpty && s.elements.isEmpty
  }

  trait InitialState[E <: Position] {
    def initialNodes: List[Node[E]]
    def initialState(q: Float2): State[E]
    def nodeQueueSizeHint: Int = 32
    def elemQueueSizeHint: Int = 32
    def foundElemSizeHint: Int = 32
  }

  object DistanceConfig {
    given euclidean: DistanceConfig = Euclidean
    given manhattan: DistanceConfig = Manhattan

    private object Euclidean extends DistanceConfig {
      override def elemDist(q: Float2, e: Position): Float = Float2.distanceSq(e.position, q)
      override def nodeDist(q: Float2, b: AABB): Float = b.distanceSq(q)
    }

    private object Manhattan extends DistanceConfig {
      override def elemDist(q: Float2, e: Position): Float = Float2.manhattan(e.position, q)
      override def nodeDist(q: Float2, b: AABB): Float = b.manhattan(q)
    }
  }

}
