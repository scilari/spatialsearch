package com.scilari.geometry.spatialsearch.searches
import com.scilari.geometry.models.{Float2, Position, Support}
import com.scilari.geometry.spatialsearch.core.IncrementalSearch
import com.scilari.geometry.spatialsearch.core.State.DefaultInitialState
import com.scilari.geometry.spatialsearch.core.State
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeStructure.Node
import com.scilari.geometry.spatialsearch.core.SearchConfig
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

object Polygonal {

  final class PolygonalImpl[E <: Position](val initialNodes: List[Node[E]])(using DistanceConfig)
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

  final class PolygonalDynamicMaxRange[E <: Position](
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

  private def isDominatedBy(e: Support, queryPoint: Float2, dominator: Float2): Boolean = {
    !e.intersectsHalfPlane(queryPoint, dominator)
  }

  private def isDominatedBy[E <: Position](
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
