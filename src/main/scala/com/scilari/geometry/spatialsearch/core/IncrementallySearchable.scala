package com.scilari.geometry.spatialsearch.core

import scala.annotation.tailrec

/**
  * Functionality for the incremental knn search described e.g. in
  * Samet: "Multidimensional and Metric Data Structures".
  * Provides highly versatile searches via modifiable SearchParameters
  */
trait IncrementallySearchable extends SearchConfig{
  type NodeType <: Tree.Node[E, NodeType]

  def search(queryPoint: Q): Seq[E] = search(initialState(queryPoint))

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
          val es = node.elements
          val n = es.length
          var i = 0
          while(i < n){
            val e = es(i)
            if(filterElements(e, state)) elements.enqueue(elemDist(queryPoint, e), e)
            i += 1
          }
        } else {
          val cs = node.children
          val n = cs.length
          var i = 0
          while(i < n){
            val c = cs(i)
            if(filterNodes(c, state)) nodes.enqueue(nodeDist(queryPoint, c), c)
            i += 1
          }
        }
      }
      search(state)
    }
  }

}
