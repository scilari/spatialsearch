package com.scilari.geometry.spatialsearch


import com.scilari.geometry.models.{Float2, HalfPlaneObject, MetricObject}
import com.scilari.geometry.spatialsearch.IncrementallySearchable.State
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.math._

/**
  * Created by iv on 1/17/2017.
  */
object Searches {
  class Knn[P, N <: Tree[P, E], E <: MetricObject[P]](k: Int) extends IncrementallySearchable[P, N, E]{
    val parameters = new IncrementallySearchable.SearchParameters[P, N, E](
      endCondition = (s: State[P, N, E]) => s.foundElements.size >= k
    )
  }

  class Range[P, N <: Tree[P, E], E <: MetricObject[P]](r: Float) extends IncrementallySearchable[P, N, E]{
    val rSq: Float = r*r
    val parameters = new IncrementallySearchable.SearchParameters[P, N, E](
      endCondition = (s: State[P, N, E]) => s.elemDistSq > rSq && s.nodeDistSq > rSq
    )
  }

  class KnnWithCondition[P, N <: Tree[P, E], E <: MetricObject[P]](k: Int, condition: E => Boolean) extends IncrementallySearchable[P, N, E]{
    val parameters = new IncrementallySearchable.SearchParameters[P, N, E](
      endCondition = (s: State[P, N, E]) => s.foundElements.size >= k,
      filterElements = (e: E, _) => condition(e)
    )
  }

  class RangeUntilFirstFound[P, N <: Tree[P, E], E <: MetricObject[P]](r: Float) extends IncrementallySearchable[P, N, E]{
    val rSq: Float = r*r
    val parameters = new IncrementallySearchable.SearchParameters[P, N, E](
      endCondition = (s: State[P, N, E]) => {
        s.foundElements.nonEmpty || (s.elemDistSq > rSq && s.nodeDistSq > rSq)
      }
    )
  }

  class Polygonal[E <: Float2] extends IncrementallySearchable[Float2, QuadTree[E], E]{
    val parameters = new IncrementallySearchable.SearchParameters[Float2, QuadTree[E], E](
      filterElements = (e: E, s: State[Float2, QuadTree[E], E]) => {
        !isDominatedBy(e, s.queryPoint, s.foundElements)
      },

      filterNodes = (n: QuadTree[E], s: State[Float2, QuadTree[E], E]) => {
        !isDominatedBy(n, s.queryPoint, s.foundElements)
      }
    )

  }

  def isDominatedBy(e: HalfPlaneObject, queryPoint: Float2, dominator: Float2): Boolean ={
    !e.intersectsHalfPlane(queryPoint, dominator)
  }

  def isDominatedBy(e: HalfPlaneObject, queryPoint: Float2, dominators: Seq[Float2]): Boolean = {
    dominators.exists(isDominatedBy(e, queryPoint, _))
  }


  def debugState[_, N <: Tree[_, E], E <: MetricObject[_]](state: State[_, N, E]): Unit ={
    println("Node queue length: " + state.nodes.size + ", closest at: " + sqrt(state.nodeDistSq))
    println("Elem queue length: " + state.elements.size + ", closest at: " + sqrt(state.elemDistSq))
    println("Found element: " + state.foundElements.size)
  }

}
