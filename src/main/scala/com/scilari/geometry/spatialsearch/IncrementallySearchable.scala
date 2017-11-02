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
trait IncrementallySearchable[P, E <: MetricObject[P]/*, TreeType <: Tree[P, E]*/] {
  //private type B = TreeType#BaseType
  //private type N = TreeType#NodeType
  //private type L = TreeType#LeafType
  type BB = Tree[P, E]#BaseType
  type NN = Tree[P, E]#NodeType
  type LL = Tree[P, E]#LeafType


  val parameters: SearchParameters

  def search(queryPoint: P, tree: BB): Seq[E] = search(initialState(queryPoint, tree))

  def search(queryPoint: P, trees: Seq[BB]): Seq[E] = search(State(queryPoint, trees))

  private[this] def initialState(queryPoint: P, tree: BB): State = {
    new State(
      queryPoint,
      new FloatHeap[Tree[P, E]#BaseType](parameters.nodeQueueSizeHint)(tree.distanceSq(queryPoint), tree),
      new FloatHeap[E](parameters.elemQueueSizeHint),
      new ArrayBuffer[E](parameters.foundElemSizeHint)
    )
  }

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
          case node: Tree[P, E]#Node /*if node.nonLeaf*/ =>
            //println("found node: " + node.isLeaf )
            node.children.foreach{ c => if (filterNodes(c, state)) nodes.enqueue(c.distanceSq(queryPoint), c) }

          case leaf: Tree[P, E]#Leaf =>
            //println("found LEAF")
            leaf.elements.foreach{ c => if (filterElements(c, state)) elements.enqueue(c.distanceSq(queryPoint), c)}


        }
      }
      search(state)
    }

  }

  final class State(
    val queryPoint: P,
    val nodes: FloatPriorityQueue[Tree[P, E]#BaseType],
    val elements: FloatPriorityQueue[E] = new FloatHeap[E](),
    var foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()
  ){

    def elemDistSq: Float = if(elements.nonEmpty) elements.headKey else Float.PositiveInfinity
    def nodeDistSq: Float = if(nodes.nonEmpty) nodes.headKey else Float.PositiveInfinity

  }

  object State{
    def apply(queryPoint: P, trees: Seq[BB]): State = {
      val initialNodes = new FloatHeap[BB]()
      trees.foreach(tree => initialNodes.enqueue(tree.distanceSq(queryPoint), tree))
      new State(queryPoint, initialNodes)
    }
  }

  class SearchParameters{
    def endCondition(s: State): Boolean = false
    def filterElements(e: E, s: State): Boolean = true
    def filterNodes(n: BB, s: State): Boolean = true
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
