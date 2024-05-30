package com.scilari.geometry.spatialsearch.searches
import com.scilari.geometry.models.{Float2, Position, Support}
import com.scilari.geometry.spatialsearch.core.IncrementalSearch
import com.scilari.geometry.spatialsearch.core.State.DefaultInitialState
import com.scilari.geometry.spatialsearch.core.State
import com.scilari.geometry.spatialsearch.quadtree.Tree.Node
import com.scilari.geometry.spatialsearch.core.SearchConfig
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig
import scala.collection.mutable.ArrayBuffer

trait PolygonalSearches[E <: Position](using DistanceConfig) {
  def initialNodes: List[Node[E]]

  def polygonalSearch(queryPoint: Float2): ArrayBuffer[E] =
    PolygonalSearches.PolygonalImpl[E](initialNodes).search(queryPoint)

  def fastPolygonalSearch(queryPoint: Float2): ArrayBuffer[E] =
    PolygonalSearches
      .PolygonalDynamicMaxRange[E](initialNodes, maxRangeFactor = 3)
      .search(queryPoint)

  def polygonalWithFilter(queryPoint: Float2, filter: E => Boolean): ArrayBuffer[E] =
    PolygonalSearches.PolygonalWithFilter[E](initialNodes, filter).search(queryPoint)

  def fastPolygonalWithFilter(queryPoint: Float2, filter: E => Boolean): ArrayBuffer[E] =
    PolygonalSearches
      .PolygonalDynamicMaxRangeWithFilter[E](initialNodes, filter, maxRangeFactor = 3)
      .search(queryPoint)
}

object PolygonalSearches {

  class PolygonalImpl[E <: Position](val initialNodes: List[Node[E]])(using DistanceConfig)
      extends SearchConfig.DefaultFiltering[E]
      with IncrementalSearch[E]
      with DefaultInitialState[E] {

    override def filterElements(e: E, s: State[E]): Boolean = {
      !isDominatedBy(e.position, s.queryPoint, s.foundElements)
    }

    override def filterNodes(n: Node[E], s: State[E]): Boolean = {
      !isDominatedBy(n.bounds, s.queryPoint, s.foundElements)
    }
  }

  final class PolygonalWithFilter[E <: Position](initialNodes: List[Node[E]], filter: E => Boolean)(
      using DistanceConfig
  ) extends PolygonalImpl[E](initialNodes) {
    override def filterElements(e: E, s: State[E]): Boolean = {
      filter(e) && super.filterElements(e, s)
    }
  }

  final class PolygonalDynamicMaxRangeWithFilter[E <: Position](
      initialNodes: List[Node[E]],
      filter: E => Boolean,
      maxRangeFactor: Float = 3.0f
  )(using
      DistanceConfig
  ) extends PolygonalDynamicMaxRange[E](initialNodes, maxRangeFactor) {
    override def filterElements(e: E, s: State[E]): Boolean = {
      filter(e) && super.filterElements(e, s)
    }
  }

  class PolygonalDynamicMaxRange[E <: Position](
      val initialNodes: List[Node[E]],
      maxRangeFactor: Float
  )(using DistanceConfig)
      extends SearchConfig.DefaultFiltering[E]
      with IncrementalSearch[E]
      with DefaultInitialState[E] {
    private[this] val rangeFactorSq: Float = maxRangeFactor * maxRangeFactor
    private[this] var maxRangeSq: Float = Float.PositiveInfinity

    override def endCondition(s: State[E]): Boolean = {
      if (maxRangeSq.isPosInfinity && !s.minElemDist.isPosInfinity) {
        maxRangeSq = s.minElemDist * rangeFactorSq
      }
      false
    }

    override def filterElements(e: E, s: State[E]): Boolean = {
      elemDist(s.queryPoint, e) <= maxRangeSq && !isDominatedBy(
        e.position,
        s.queryPoint,
        s.foundElements
      )
    }

    override def filterNodes(n: Node[E], s: State[E]): Boolean = {
      nodeDist(s.queryPoint, n.bounds) <= maxRangeSq && !isDominatedBy(
        n.bounds,
        s.queryPoint,
        s.foundElements
      )
    }

  }

  private inline def isDominatedBy(e: Support, queryPoint: Float2, dominator: Float2): Boolean = {
    !e.intersectsHalfPlane(queryPoint, dominator)
  }

  private inline def isDominatedBy[E <: Position](
      e: Support,
      queryPoint: Float2,
      dominators: collection.Seq[E]
  ): Boolean = {
    // Going through in reverse order, as more recently added points are more likely to dominate
    var i = dominators.size - 1
    while (i >= 0) {
      if (isDominatedBy(e, queryPoint, dominators(i).position)) i = -1
      i -= 1
    }
    i < -1
  }

}
