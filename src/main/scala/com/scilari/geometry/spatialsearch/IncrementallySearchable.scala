package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.MetricObject
import com.scilari.geometry.spatialsearch.IncrementallySearchable._
import com.scilari.geometry.spatialsearch.queues._

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
  private type B = Tree[P, E]#BaseType
  private type N = Tree[P, E]#NodeType
  private type L = Tree[P, E]#LeafType

  val parameters: SearchParameters[P, E]

  def search(queryPoint: P, tree: B): Seq[E] = search(initialState(queryPoint, tree))

  def search(queryPoint: P, trees: Seq[B]): Seq[E] = search(State(queryPoint, trees))

  private[this] def initialState(queryPoint: P, tree: B): State[P, E] = {
    new State[P, E](
      queryPoint,
      new FloatHeap[B](parameters.nodeQueueSizeHint)(tree.distanceSq(queryPoint), tree),
      new FloatHeap[E](parameters.elemQueueSizeHint),
      new ArrayBuffer[E](parameters.foundElemSizeHint)
    )
  }

  @tailrec
  final def search(state: State[P, E], params: SearchParameters[P, E] = parameters): Seq[E] = {
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

}

object IncrementallySearchable{

  final class State[P, E](
    val queryPoint: P,
    val nodes: FloatPriorityQueue[Tree[P, E]#BaseType],
    val elements: FloatPriorityQueue[E] = new FloatHeap[E](),
    var foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()
  ){

    def elemDistSq: Float = if(elements.nonEmpty) elements.headKey else Float.PositiveInfinity
    def nodeDistSq: Float = if(nodes.nonEmpty) nodes.headKey else Float.PositiveInfinity

  }

  object State{
    def apply[P, E](queryPoint: P, trees: Seq[Tree[P, E]#BaseType]): State[P, E] = {
      val initialNodes = new FloatHeap[Tree[P, E]#BaseType]()
      trees.foreach(tree => initialNodes.enqueue(tree.distanceSq(queryPoint), tree))
      new State(queryPoint, initialNodes)
    }
  }

  class SearchParameters[P, E]{
    def endCondition(s: State[P, E]): Boolean = false
    def filterElements(e: E, s: State[P, E]): Boolean = true
    def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean = true
    def modifyState(s: State[P, E]): Unit = ()
    val nodeQueueSizeHint: Int = 32
    val elemQueueSizeHint: Int = 32
    val foundElemSizeHint: Int = 32
  }



}
