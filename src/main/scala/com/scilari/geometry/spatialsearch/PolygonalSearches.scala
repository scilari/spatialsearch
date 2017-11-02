package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{Float2, HalfPlaneObject, MetricObject}
import com.scilari.geometry.spatialsearch.IncrementallySearchable.{SearchParameters, State}

trait PolygonalSearches[P <: Float2, E <: Float2 with MetricObject[P]]{

  final class Polygonal extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters[P, E] = PolygonalParameters

    private final object PolygonalParameters extends SearchParameters[P, E]{
      override def filterElements(e: E, s: State[P, E]): Boolean = {
        !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: Tree[P,E]#BaseType, s: State[P, E]): Boolean = {
        !isNodeDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }

  final class PolygonalMaxRange(r: Float) extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters[P, E] = PolygonalParameters
    private[this] val rSq = r*r

    private[this] final object PolygonalParameters extends SearchParameters[P, E]{

      override def filterElements(e: E, s: State[P, E]): Boolean = {
        s.queryPoint.distanceSq(e) <= rSq && !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean = {
        n.distanceSq(s.queryPoint) <= rSq && !isNodeDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }


  object Polygonal{
    def isDominatedBy(e: HalfPlaneObject, queryPoint: Float2, dominator: Float2): Boolean ={
      !e.intersectsHalfPlane(queryPoint, dominator)
    }

    def isNodeDominatedBy(node: Any, queryPoint: Float2, dominators: Seq[Float2]): Boolean ={
      isDominatedBy(
        node.asInstanceOf[HalfPlaneObject], queryPoint, dominators) // TODO: get rid of this cast
    }

    def isDominatedBy(e: HalfPlaneObject, queryPoint: Float2, dominators: Seq[Float2]): Boolean = {
      // Going through in reverse order, as more recently added points are more likely to dominate
      var i = dominators.size - 1
      while(i >= 0){
        if(isDominatedBy(e, queryPoint, dominators(i))) return true
        i -= 1
      }
      false
    }
  }

  final class PolygonalDynamicMaxRange(maxRangeFactor: Float) extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters[P, E] = PolygonalParameters
    private[this] val rangeFactorSq = maxRangeFactor*maxRangeFactor
    private[this] var firstElementDistSq = Float.PositiveInfinity
    private[this] var maxRange = Float.PositiveInfinity


    private[this] final object PolygonalParameters extends SearchParameters[P, E]{
      override def modifyState(s: State[P, E]): Unit = {
        if(firstElementDistSq > s.elemDistSq){
          firstElementDistSq = s.elemDistSq
          maxRange = firstElementDistSq*rangeFactorSq
        }
      }

      override def filterElements(e: E, s: State[P, E]): Boolean = {
        e.distanceSq(s.queryPoint) <= maxRange && !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean = {
        n.distanceSq(s.queryPoint) <= maxRange && !isNodeDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }

}
