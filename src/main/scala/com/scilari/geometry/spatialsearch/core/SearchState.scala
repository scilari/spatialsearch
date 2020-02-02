package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.spatialsearch.core.SearchConfig.InitialState
import com.scilari.geometry.spatialsearch.queues.{FloatHeap, FloatPriorityQueue}
import com.scilari.math.sqrt

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

final class SearchState[Q, E, NodeType](
  val queryPoint: Q,
  val nodes: FloatPriorityQueue[NodeType],
  val elements: FloatPriorityQueue[E], // = new FloatHeap[E](),
  var foundElements: mutable.Buffer[E] // = new ArrayBuffer[E]()
){

  def minElemDist: Float = if(elements.nonEmpty) elements.headKey else Float.PositiveInfinity
  def minNodeDist: Float = if(nodes.nonEmpty) nodes.headKey else Float.PositiveInfinity

}

object SearchState{
  trait DefaultInitialState extends InitialState {
    var root: NodeType
    override val foundElemSizeHint: Int = 8

    def initialState(q: Q): SearchState[Q, E, NodeType] = {
      new SearchState[Q, E, NodeType](
        q,
        FloatHeap[NodeType](0, root, 7),
        new FloatHeap[E](15),
        new ArrayBuffer[E](foundElemSizeHint)
      )
    }
  }


  def debugState(state: SearchState[_, _, _]): String ={
    ("Node queue length: " + state.nodes.size + ", closest at: " + sqrt(state.minNodeDist)) + "\n" +
      ("Elem queue length: " + state.elements.size + ", closest at: " + sqrt(state.minElemDist)) + "\n" +
      ("Found elements: " + state.foundElements.size)
  }
}

