package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.SearchConfig.InitialState
import com.scilari.geometry.spatialsearch.queues.FloatMinHeap
import com.scilari.math.FloatMath.sqrt
import com.scilari.geometry.spatialsearch.quadtree.Tree.Node

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

final class State[E <: Position](
    val queryPoint: Float2,
    val nodes: FloatMinHeap[Node[E]],
    val elements: FloatMinHeap[E],
    var foundElements: ArrayBuffer[E]
) {

  def minElemDist: Float = if (elements.nonEmpty) elements.minKey else Float.MaxValue
  def minNodeDist: Float = if (nodes.nonEmpty) nodes.minKey else Float.MaxValue
  def elemCloserThanNode: Boolean = minElemDist <= minNodeDist
}

object State {
  trait DefaultInitialState[E <: Position] extends InitialState[E] {
    override val foundElemSizeHint: Int = 8

    def initialState(q: Float2): State[E] = {
      new State[E](
        q,
        FloatMinHeap[Node[E]](0, initialNodes, 4),
        new FloatMinHeap[E](31),
        new ArrayBuffer[E](foundElemSizeHint)
      )
    }
  }

  def debugState(state: State[_]): String = {
    ("Node queue length: " + state.nodes.size + ", closest at: " + sqrt(state.minNodeDist)) + "\n" +
      ("Elem queue length: " + state.elements.size + ", closest at: " + sqrt(
        state.minElemDist
      )) + "\n" +
      ("Found elements: " + state.foundElements.size)
  }
}
