package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.{Position, Float2}
import com.scilari.geometry.spatialsearch.core.SearchConfig.{DistanceConfig, InitialState, SearchParameters}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Node

/**
  * Three orthogonal traits to define searches.
  * - DistanceConfig to switch e.g. between Euclidean and Manhattan distance
  * - SearchParameters to define the search control flow
  * - InitialState to initialize with root or multiple nodes, for example
  */
trait SearchConfig[Q, E <: Position] 
  extends DistanceConfig[Q, E] with SearchParameters[Q, E] with InitialState[Q, E]

object SearchConfig{

  trait DefaultFiltering[Q, E <: Position] extends SearchConfig[Q, E] {
    def addFoundElement(e: E, state: State): Unit = if (filterElements(e, state)) state.foundElements += e
    def enqueueElement(e: E, state: State): Unit = if (filterElements(e, state)) state.elements.enqueue(elemDist(state.queryPoint, e), e)
    def enqueueNode(n: Node[E], state: State): Unit = if (filterNodes(n, state)) state.nodes.enqueue(nodeDist(state.queryPoint, n), n)
    def collectFoundOrDone(s: State): Boolean = {
      while (s.elemCloserThanNode && !defaultEndCondition(s) && !endCondition(s)) {
        val candidate = s.elements.dequeueValue()
        addFoundElement(candidate, s)
      }
      defaultEndCondition(s) || endCondition(s)    
    }
  }

  trait NonFiltering[Q, E <: Position] extends SearchConfig[Q, E] {
    def addFoundElement(candidate: E, state: State): Unit =
      state.foundElements += candidate

    def enqueueElement(e: E, state: State): Unit = {
      state.elements.enqueue(elemDist(state.queryPoint, e), e)
    }

    def enqueueNode(n: Node[E], state: State): Unit = {
      state.nodes.enqueue(nodeDist(state.queryPoint, n), n)
    }
  }
  
  trait DistanceConfig[Q, E <: Position] {
    def elemDist(q: Q, e: E): Float
    def nodeDist(q: Q, n: Node[E]): Float
  }
  
  trait SearchParameters[Q, E <: Position] {
    type State = SearchState[Q, E]
    def endCondition(s: State): Boolean = false
    def filterElements(e: E, s: State): Boolean = true
    def filterNodes(n: Node[E], s: State): Boolean = true
    def addFoundElement(e: E, state: State): Unit
    def enqueueElement(e: E, state: State): Unit
    def enqueueNode(n: Node[E], state: State): Unit
    def collectFoundOrDone(s: State): Boolean
    def defaultEndCondition(s: State) = s.nodes.isEmpty && s.elements.isEmpty
  }
  
  trait InitialState[Q, E <: Position] {
    def initialState(q: Q): SearchState[Q, E]
    def nodeQueueSizeHint: Int = 32
    def elemQueueSizeHint: Int = 32
    def foundElemSizeHint: Int = 32
  }

  object DistanceConfig {
    trait Euclidean[E <: Position] extends DistanceConfig[Float2, E] {
      override def elemDist(q: Float2, e: E): Float = Float2.distanceSq(e.position, q)
      override def nodeDist(q: Float2, b: Node[E]): Float = b.bounds.distanceSq(q)
    }

    trait Manhattan[E <: Position] extends DistanceConfig[Float2, E] {
      override def elemDist(q: Float2, e: E): Float = Float2.manhattan(e.position, q)
      override def nodeDist(q: Float2, b: Node[E]): Float = b.bounds.manhattan(q)
    }
  }
  
}
