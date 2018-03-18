package com.scilari.geometry.spatialsearch

import com.scilari.geometry.spatialsearch.queues._
import com.scilari.math.sqrt

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Functionality for the incremental knn search described e.g. in
  * Samet: "Multidimensional and Metric Data Structures".
  * Provides highly versatile searches via modifiable SearchParameters
  * Created by iv on 1/17/2017.
  */
trait IncrementallySearchable[P, E] extends Tree[P, E]{

  type SearchFn = (P, BaseType) => Seq[E]

  def elemDist(p: P, e: E): Float
  def nodeDist(p: P, n: BaseType): Float

  def search(params: SearchParameters)(queryPoint: P, tree: BaseType): Seq[E] =
    search(State.defaultInitialState(queryPoint, tree, params), params)

  @tailrec
  final def search(state: State, params: SearchParameters): Seq[E] = {
    import params._
    import state._

    modifyState(state)

    if (endCondition(state) || (elements.isEmpty && nodes.isEmpty)) {
      foundElements
    } else {
      if (headNodeDist >= headElemDist) {
        val candidate = elements.dequeueValue()
        if (filterElements(candidate, state)) foundElements = foundElements += candidate
      } else {
        nodes.dequeueValue() match {
          case node: Node =>
            node.children.foreach { c => if (filterNodes(c, state)) nodes.enqueue(nodeDist(queryPoint, c), c) }

          case leaf: Leaf =>
            leaf.elements.foreach { c => if (filterElements(c, state)) elements.enqueue(elemDist(queryPoint, c), c) }
        }
      }
      search(state, params)
    }
  }


  final class State(
    val queryPoint: P,
    val nodes: FloatPriorityQueue[BaseType],
    val elements: FloatPriorityQueue[E] = new FloatHeap[E](),
    var foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()
  ){

    def headElemDist: Float = if(elements.nonEmpty) elements.headKey else Float.PositiveInfinity
    def headNodeDist: Float = if(nodes.nonEmpty) nodes.headKey else Float.PositiveInfinity

  }

  object State{
    def apply(queryPoint: P, trees: Seq[BaseType]): State = {
      val initialNodes = new FloatHeap[BaseType]()
      trees.foreach(tree => initialNodes.enqueue(nodeDist(queryPoint, tree), tree))
      new State(queryPoint, initialNodes)
    }

    def defaultInitialState(queryPoint: P, tree: BaseType, params: SearchParameters): State = {
      new State(
        queryPoint,
        new FloatHeap[BaseType](params.nodeQueueSizeHint)(nodeDist(queryPoint, tree), tree),
        new FloatHeap[E](params.elemQueueSizeHint),
        new ArrayBuffer[E](params.foundElemSizeHint)
      )
    }
  }

  class SearchParameters{
    def endCondition(s: State): Boolean = false // linter:ignore UnusedParameter
    def filterElements(e: E, s: State): Boolean = true // linter:ignore UnusedParameter
    def filterNodes(n: BaseType, s: State): Boolean = true // linter:ignore UnusedParameter
    def modifyState(s: State): Unit = () // linter:ignore UnusedParameter
    val nodeQueueSizeHint: Int = 32
    val elemQueueSizeHint: Int = 32
    val foundElemSizeHint: Int = 32
  }

  def debugState(state: State): String ={
    ("Node queue length: " + state.nodes.size + ", closest at: " + sqrt(state.headNodeDist)) + "\n" +
      ("Elem queue length: " + state.elements.size + ", closest at: " + sqrt(state.headElemDist)) + "\n" +
      ("Found elements: " + state.foundElements.size)
  }


}
