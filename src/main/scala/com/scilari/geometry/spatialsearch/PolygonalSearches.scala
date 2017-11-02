package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2, HalfPlaneObject, MetricObject}


trait PolygonalSearches[P <: Float2, E <: Float2 with MetricObject[P]]{

  final class Polygonal extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters = PolygonalParameters

    private final object PolygonalParameters extends SearchParameters{
      override def filterElements(e: E, s: State): Boolean = {
        !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: B, s: State): Boolean = {
        !isNodeDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }

  final class PolygonalMaxRange(r: Float) extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters = PolygonalParameters
    private[this] val rSq = r*r

    private[this] final object PolygonalParameters extends SearchParameters{

      override def filterElements(e: E, s: State): Boolean = {
        s.queryPoint.distanceSq(e) <= rSq && !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: B, s: State): Boolean = {
        n.asInstanceOf[AABB].distanceSq(s.queryPoint) <= rSq && !isNodeDominatedBy(n, s.queryPoint, s.foundElements)
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
    val parameters: SearchParameters = PolygonalParameters
    private[this] val rangeFactorSq = maxRangeFactor*maxRangeFactor
    private[this] var firstElementDistSq = Float.PositiveInfinity
    private[this] var maxRange = Float.PositiveInfinity


    private[this] final object PolygonalParameters extends SearchParameters{
      override def modifyState(s: State): Unit = {
        if(firstElementDistSq > s.elemDistSq){
          firstElementDistSq = s.elemDistSq
          maxRange = firstElementDistSq*rangeFactorSq
        }
      }

      override def filterElements(e: E, s: State): Boolean = {
        e.distanceSq(s.queryPoint) <= maxRange && !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: B, s: State): Boolean = {
        n.asInstanceOf[AABB].distanceSq(s.queryPoint) <= maxRange && !isNodeDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }

}
