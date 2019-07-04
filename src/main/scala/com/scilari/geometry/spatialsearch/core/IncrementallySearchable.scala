package com.scilari.geometry.spatialsearch.core

import scala.annotation.tailrec

/**
  * Functionality for the incremental knn search described e.g. in
  * Samet: "Multidimensional and Metric Data Structures".
  * Provides highly versatile searches via modifiable SearchParameters
  * Created by iv on 1/17/2017.
  */
trait IncrementallySearchable extends SearchConfig{
  type NodeType <: Tree.Node[E, NodeType]

  def search(queryPoint: Q): Seq[E] = search(initialState(queryPoint))

  // scalastyle:off
  @tailrec
  final def search(state: State): Seq[E] = {
    import state._

    modifyState(state)

    if (endCondition(state) || (elements.isEmpty && nodes.isEmpty)) {
      foundElements
    } else {
      if (minNodeDist >= minElemDist) {
        val candidate = elements.dequeueValue()
        if (filterElements(candidate, state)) foundElements += candidate
      } else {
        val node = nodes.dequeueValue()
        if(node.isLeaf){
          var i = 0; val n = node.elements.size
          while (i < n){
            val c = node.elements(i)
            if(filterElements(c, state)) elements.enqueue(elemDist(queryPoint, c), c)
            i += 1
          }
        } else {
          var i = 0; val n = node.childCount
          while (i < n){
            val c = node.children(i)
            if(filterNodes(c, state)) nodes.enqueue(nodeDist(queryPoint, c), c)
            i += 1
          }
        }
      }
      search(state)
    }
  }

}
