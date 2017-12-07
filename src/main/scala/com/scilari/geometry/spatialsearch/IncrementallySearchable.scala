package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.MetricObject
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
trait IncrementallySearchable[P, E <: MetricObject[P]] {

  type B = Tree[P, E]#BaseType
  type N = Tree[P, E]#NodeType
  type L = Tree[P, E]#LeafType

  val parameters: SearchParameters

  def search(queryPoint: P, tree: B): Seq[E] = search(State.defaultInitialState(queryPoint, tree))

  @tailrec
  final def search(state: State, params: SearchParameters = parameters): Seq[E] = {
    import state._
    import params._

    modifyState(state)

    if (endCondition(state) || (elements.isEmpty && nodes.isEmpty))
      foundElements
    else {
      if (nodeDistSq >= elemDistSq) {
        val candidate = elements.dequeueValue()
        if (filterElements(candidate, state)) foundElements = foundElements += candidate
      } else {
        nodes.dequeueValue() match {
          case node: Tree[P, E]#Node =>
            node.children.foreach{ c => if (filterNodes(c, state)) nodes.enqueue(c.distanceSq(queryPoint), c) }

          case leaf: Tree[P, E]#Leaf =>
            leaf.elements.foreach{ c => if (filterElements(c, state)) elements.enqueue(c.distanceSq(queryPoint), c)}


        }
      }
      search(state)
    }

  }

  final class State(
    val queryPoint: P,
    val nodes: FloatPriorityQueue[B],
    val elements: FloatPriorityQueue[E] = new FloatHeap[E](),
    var foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()
  ){

    def elemDistSq: Float = if(elements.nonEmpty) elements.headKey else Float.PositiveInfinity
    def nodeDistSq: Float = if(nodes.nonEmpty) nodes.headKey else Float.PositiveInfinity

  }

  object State{
    def apply(queryPoint: P, trees: Seq[B]): State = {
      val initialNodes = new FloatHeap[B]()
      trees.foreach(tree => initialNodes.enqueue(tree.distanceSq(queryPoint), tree))
      new State(queryPoint, initialNodes)
    }

    def defaultInitialState(queryPoint: P, tree: B): State = {
      new State(
        queryPoint,
        new FloatHeap[B](parameters.nodeQueueSizeHint)(tree.distanceSq(queryPoint), tree),
        new FloatHeap[E](parameters.elemQueueSizeHint),
        new ArrayBuffer[E](parameters.foundElemSizeHint)
      )
    }
  }

  class SearchParameters{
    def endCondition(s: State): Boolean = false
    def filterElements(e: E, s: State): Boolean = true
    def filterNodes(n: B, s: State): Boolean = true
    def modifyState(s: State): Unit = ()
    val nodeQueueSizeHint: Int = 32
    val elemQueueSizeHint: Int = 32
    val foundElemSizeHint: Int = 32
  }

  def debugState(state: State): Unit ={
    println("Node queue length: " + state.nodes.size + ", closest at: " + sqrt(state.nodeDistSq))
    println("Elem queue length: " + state.elements.size + ", closest at: " + sqrt(state.elemDistSq))
    println("Found elements: " + state.foundElements.size)
  }


}
