package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.{AABB, Float2, ExtremePoint}
import com.scilari.geometry.spatialsearch.core.IncrementallySearchable


trait PolygonalSearches[P <: Float2, E <: Float2] extends IncrementallySearchable[P, E]{
  import Polygonal._
  type BaseType <: Base with ExtremePoint

  def polygonal: SearchFn = search(new PolygonalParameters)

  def polygonalDynamicMaxRange(maxRangeFactor: Float = 3.0f): SearchFn = search(new PolygonalDynamicMaxRange(maxRangeFactor))

  final class PolygonalParameters extends SearchParameters{
    override def filterElements(e: E, s: State): Boolean = {
      !isDominatedBy(e, s.queryPoint, s.foundElements)
    }

    override def filterNodes(n: BaseType, s: State): Boolean = {
      !isDominatedBy(n, s.queryPoint, s.foundElements)
    }

    override val foundElemSizeHint: Int = 8
  }

  final class PolygonalDynamicMaxRange(maxRangeFactor: Float) extends SearchParameters{
    private[this] val rangeFactorSq = maxRangeFactor*maxRangeFactor
    private[this] var firstElementDistSq = Float.PositiveInfinity
    private[this] var maxRange = Float.PositiveInfinity

    override def modifyState(s: State): Unit = {
      if(firstElementDistSq > s.headElemDist){
        firstElementDistSq = s.headElemDist
        maxRange = firstElementDistSq*rangeFactorSq
      }
    }

    override def filterElements(e: E, s: State): Boolean = {
      elemDist(s.queryPoint, e) <= maxRange && !isDominatedBy(e, s.queryPoint, s.foundElements)
    }

    override def filterNodes(n: BaseType, s: State): Boolean = {
      nodeDist(s.queryPoint, n) <= maxRange && !isDominatedBy(n, s.queryPoint, s.foundElements)
    }

    override val foundElemSizeHint: Int = 8
  }

  private object Polygonal{
    def isDominatedBy(e: ExtremePoint, queryPoint: Float2, dominator: Float2): Boolean ={
      !e.intersectsHalfPlane(queryPoint, dominator)
    }

    def isDominatedBy(e: ExtremePoint, queryPoint: Float2, dominators: Seq[Float2]): Boolean = {
      // Going through in reverse order, as more recently added points are more likely to dominate
      var i = dominators.size - 1
      while(i >= 0) {
        if (isDominatedBy(e, queryPoint, dominators(i))) i = -1
        i -= 1
      }
      i < -1
    }
  }

}







