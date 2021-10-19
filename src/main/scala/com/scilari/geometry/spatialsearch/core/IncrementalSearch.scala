package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.queues.FloatMinHeap
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Node

import scala.annotation.tailrec
import scala.collection.mutable.Buffer


/**
 * Functionality for the incremental knn search described e.g. in
 * Samet: "Multidimensional and Metric Data Structures".
 * Provides highly versatile searches via modifiable search configurations.
 */
trait IncrementalSearch[Q, E <: Position] extends SearchConfig[Q, E]  {

  def search(queryPoint: Q): collection.Seq[E] = search(initialState(queryPoint))

  protected def addFoundElement(e: E, state: State): Unit = {
    if(filterElements(e, state)) state.foundElements += e
  }

  protected def enqueueElement(e: E, state: State): Unit = {
    if (filterElements(e, state)) state.elements.enqueue(elemDist(state.queryPoint, e), e)
  }

  protected def enqueueNode(n: Node[E], state: State): Unit = {
    if (filterNodes(n, state)) state.nodes.enqueue(nodeDist(state.queryPoint, n), n)
  }

  @tailrec
  private[this] def search(state: State): collection.Seq[E] = {
    import state._

    
    if (endCondition(state)) {
      foundElements
    } else {
      val node = nodes.dequeueValue()
      if(node.isLeaf){
        val es = node.elements
        val n = es.length
        var i = 0
        while (i < n) {
          val e = es(i)
          enqueueElement(e, state)
          i += 1
        }
      } else {
        val cs = node.children
        val n = cs.length
        var i = 0
        while (i < n) {
          val n = cs(i)
          enqueueNode(n, state)
          i += 1
        }
      }

      search(state)
    }
  }



  @tailrec
  private[this] def search2(state: State): collection.Seq[E] = {
    import state._

    elemCloserThanNode = minNodeDist >= minElemDist

    if (endCondition(state) || (elements.isEmpty && nodes.isEmpty)) {
      foundElements
    } else {
      if (elemCloserThanNode) {
        val candidate = elements.dequeueValue()
        addFoundElement(candidate, state)
      } else {
        val node = nodes.dequeueValue()
        if(node.isLeaf){
          val es = node.elements
          val n = es.length
          var i = 0
          while (i < n) {
            val e = es(i)
            enqueueElement(e, state)
            i += 1
          }
        } else {
          val cs = node.children
          val n = cs.length
          var i = 0
          while (i < n) {
            val n = cs(i)
            enqueueNode(n, state)
            i += 1
          }
        }
      }
      search2(state)
    }
  }

}

object IncrementalSearch {

  trait NonFilteringIncrementalSearch[Q, E <: Position] extends IncrementalSearch[Q, E]{
    override inline def addFoundElement(candidate: E, state: State): Unit =
      state.foundElements += candidate

    override inline def enqueueElement(e: E, state: State): Unit = {
      state.elements.enqueue(elemDist(state.queryPoint, e), e)
    }

    override inline def enqueueNode(n: Node[E], state: State): Unit = {
      state.nodes.enqueue(nodeDist(state.queryPoint, n), n)
    }
  }


}
