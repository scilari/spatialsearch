package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{Float2, HasPosition, Support}
import com.scilari.geometry.spatialsearch.core.IncrementallySearchable
import com.scilari.geometry.spatialsearch.core.SearchState.DefaultInitialState
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.QuadNode


object Polygonal{

  final class PolygonalImpl[EE <: HasPosition](var root: QuadNode[EE])
    extends IncrementallySearchable with EuclideanTypes[EE] with DefaultInitialState {

    override def filterElements(e: E, s: State): Boolean = {
      !isDominatedBy(e.position, s.queryPoint, s.foundElements)
    }

    override def filterNodes(n: NodeType, s: State): Boolean = {
      !isDominatedBy(n.bounds, s.queryPoint, s.foundElements)
    }
  }

  final class PolygonalDynamicMaxRange[EE <: HasPosition](var root: QuadNode[EE], maxRangeFactor: Float)
    extends IncrementallySearchable with EuclideanTypes[EE] with DefaultInitialState {
    private[this] val rangeFactorSq: Float = maxRangeFactor*maxRangeFactor
    //private[this] var firstElementDistSq: Float = Float.PositiveInfinity
    private[this] var maxRangeSq: Float = Float.PositiveInfinity

    override def modifyState(s: State): Unit = {
      if(maxRangeSq.isPosInfinity && !s.minElemDist.isPosInfinity){
        //firstElementDistSq = s.minElemDist
        maxRangeSq = s.minElemDist*rangeFactorSq
      }
    }

    override def filterElements(e: E, s: State): Boolean = {
      elemDist(s.queryPoint, e) <= maxRangeSq && !isDominatedBy(e.position, s.queryPoint, s.foundElements)
    }

    override def filterNodes(n: NodeType, s: State): Boolean = {
      nodeDist(s.queryPoint, n) <= maxRangeSq && !isDominatedBy(n.bounds, s.queryPoint, s.foundElements)
    }

  }

  private def isDominatedBy(e: Support, queryPoint: Float2, dominator: Float2): Boolean = {
    !e.intersectsHalfPlane(queryPoint, dominator)
  }

  private def isDominatedBy[E <: HasPosition](e: Support, queryPoint: Float2, dominators: Seq[E]): Boolean = {
    // Going through in reverse order, as more recently added points are more likely to dominate
    var i = dominators.size - 1
    while(i >= 0) {
      if (isDominatedBy(e, queryPoint, dominators(i).position)) i = -1
      i -= 1
    }
    i < -1
  }


}


