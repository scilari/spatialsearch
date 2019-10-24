package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.spatialsearch.core.SearchConfig.{DistanceConfig, InitialState, SearchParameters}

/**
  * Three orthogonal traits to define searches.
  * - DistanceConfig to switch e.g. between Euclidean and Manhattan distance
  * - SearchParameters to define the search control flow
  * - InitialState to initialize with root or multiple nodes, for example
  */
trait SearchConfig extends DistanceConfig with SearchParameters with InitialState

object SearchConfig{
  trait DistanceConfig extends Types {
    def elemDist(q: Q, e: E): Float
    def nodeDist(q: Q, n: NodeType): Float
  }

  trait SearchParameters extends Types {
    type State = SearchState[Q, E, NodeType]
    def endCondition(s: State): Boolean = false // linter:ignore UnusedParameter
    def filterElements(e: E, s: State): Boolean = true // linter:ignore UnusedParameter
    def filterNodes(n: NodeType, s: State): Boolean = true // linter:ignore UnusedParameter
    def modifyState(s: State): Unit = () // linter:ignore UnusedParameter
  }

  trait InitialState extends Types {
    def initialState(q: Q): SearchState[Q, E, NodeType]

    def nodeQueueSizeHint: Int = 32
    def elemQueueSizeHint: Int = 32
    def foundElemSizeHint: Int = 32
  }


}
