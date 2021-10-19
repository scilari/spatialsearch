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
  
  @tailrec
  private[this] def search(state: State): collection.Seq[E] = {
    import state._

    if (collectFoundOrDone(state)) {
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

}
