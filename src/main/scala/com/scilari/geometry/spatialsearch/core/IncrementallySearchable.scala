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
    @inline def handleElem(e: E): Unit = if(filterElements(e, state)) elements.enqueue(elemDist(queryPoint, e), e)
    @inline def handleNode(n: NodeType): Unit = if(filterNodes(n, state)) nodes.enqueue(nodeDist(queryPoint, n), n)

    modifyState(state)

    if (endCondition(state) || (elements.isEmpty && nodes.isEmpty)) {
      foundElements
    } else {
      if (minNodeDist >= minElemDist) {
        val candidate = elements.dequeueValue()
        if (filterElements(candidate, state)) foundElements += candidate
      } else {
        val node = nodes.dequeueValue()
        node.forEachElement(handleElem)
        node.forEachChild(handleNode)
      }
      search(state)
    }
  }

}
