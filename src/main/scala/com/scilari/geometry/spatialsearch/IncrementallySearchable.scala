package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{MetricObject}
import com.scilari.geometry.spatialsearch.IncrementallySearchable._
import com.scilari.geometry.spatialsearch.Tree._

import scala.annotation.tailrec
import scala.collection.mutable


/**
  * Functionality for the incremental knn search described e.g. in
  * Samet: "Multidimensional and Metric Data Structures".
  * Provides highly versatile searches via modifiable SearchParameters
  * Created by iv on 1/17/2017.
  */
trait IncrementallySearchable[P, N <: Tree[P], E <: MetricObject[P]] {
  val parameters: SearchParameters[P, N, E]

  def search(queryPoint: P, tree: N): Seq[E] = search(queryPoint, tree, parameters)

  private[this] def search(queryPoint: P, tree: N, params: SearchParameters[P, N, E]): Seq[E] = {

    val initialState = new State(
      queryPoint = queryPoint,
      elements = new mutable.PriorityQueue[(Float, E)]()(new OrderingByDistanceKey[E]),
      nodes = mutable.PriorityQueue[(Float, N)]((tree.distanceSq(queryPoint), tree))(new OrderingByDistanceKey[N]),
      foundElements = mutable.Buffer[E]()
    )

    search(initialState, params)

  }

  @tailrec
  private[this] def search(state: State[P, N, E], params: SearchParameters[P, N, E]): Seq[E] = {
    import state._
    import params._

    modifyState(state)

    if (endCondition(state) || (elements.isEmpty && nodes.isEmpty))
      foundElements
    else {
      if (nodeDistSq >= elemDistSq) {
        val candidate = elements.dequeue()._2
        if (filterElements(candidate, state)) foundElements += candidate
      } else {
        nodes.dequeue()._2 match {
          case l: (Leaf[P, E]) => {
            l.children.foreach{ c => if (params.filterElements(c, state)) elements.enqueue((c.distanceSq(queryPoint), c))}
          }
          case n: Node[P, N] => {
            n.children.foreach{ c => if (params.filterNodes(c, state)) nodes.enqueue((c.distanceSq(queryPoint), c)) }
          }
        }
      }
      search(state, params)
    }

  }

}

object IncrementallySearchable{
  class State[P, N <: Tree[P], E <: MetricObject[P]](
    val queryPoint: P,
    val elements: mutable.PriorityQueue[(Float, E)],
    val nodes: mutable.PriorityQueue[(Float, N)],
    val foundElements: mutable.Buffer[E]
  ){

    def elemDistSq = if(elements.nonEmpty) elements.head._1 else Float.PositiveInfinity
    def nodeDistSq = if(nodes.nonEmpty) nodes.head._1 else Float.PositiveInfinity

  }

  class SearchParameters[P, N <: Tree[P], E <: MetricObject[P]](
    val endCondition: State[P, N, E] => Boolean = (_: State[P, N, E]) => false,
    val filterElements: (E, State[P, N, E]) => Boolean = (_: E, _: State[P, N, E]) => true,
    val filterNodes: (N, State[P, N, E]) => Boolean = (_: Tree[P], _: State[P, N, E]) => true,
    val modifyState: State[P, N, E] => Unit = (_: State[P, N, E]) => ()
  )

  class OrderingByDistanceKey[T] extends Ordering[(Float, T)]{
    def compare(o1: (Float, T), o2: (Float, T)): Int =
      scala.math.Ordering.Float.compare(o2._1, o1._1)
  }


  
}
