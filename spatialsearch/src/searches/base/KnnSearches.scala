package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.State.DefaultInitialState
import com.scilari.geometry.spatialsearch.core.{IncrementalSearch, SearchConfig, State}
import com.scilari.geometry.spatialsearch.core.SearchConfig.{InitialState, SearchParameters}
import com.scilari.geometry.spatialsearch.quadtree.Tree.Node
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.queues.{FloatMaxHeapK, FloatMinHeap}

import scala.collection.mutable.ArrayBuffer
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

trait KnnSearches[E <: Position](using DistanceConfig) {

  def initialNodes: List[Node[E]]
  import KnnSearches._

  def knnSearch(queryPoint: Float2, k: Int): ArrayBuffer[E] =
    KnnImpl(initialNodes, k).search(queryPoint)

  final class KnnImpl[E <: Position](val initialNodes: List[Node[E]], val k: Int) extends Knn[E]

  def knnSearchWithFilter(queryPoint: Float2, k: Int, filter: E => Boolean): ArrayBuffer[E] =
    KnnWithFilterImpl[E](initialNodes, k, filter).search(queryPoint)

  def knnWithinRadius(queryPoint: Float2, k: Int, r: Float): ArrayBuffer[E] =
    KnnWithinRadiusImpl(initialNodes, k, r).search(queryPoint)

  def knnWithinSector(
      queryPoint: Float2,
      k: Int,
      sectorDir: Float,
      sectorWidth: Float,
      r: Float = Float.PositiveInfinity
  ): ArrayBuffer[E] =
    KnnWithinSectorImpl(initialNodes, k, sectorDir, sectorWidth, 0f, r).search(queryPoint)

  def beamSearch(
      queryPoint: Float2,
      dir: Float,
      beamWidth: Float,
      beamLength: Float = Float.PositiveInfinity,
      k: Int = 1
  ): ArrayBuffer[E] =
    KnnWithinSectorImpl(
      initialNodes,
      k = k,
      sectorDir = dir,
      sectorWidth = 0f,
      hitboxSize = beamWidth,
      r = beamLength
    ).search(queryPoint)

  final class KnnWithFilterImpl[E <: Position](
      val initialNodes: List[Node[E]],
      k: Int,
      filter: E => Boolean
  ) extends KnnWithFilter[E](k, filter)

  final class KnnWithinRadiusImpl[E <: Position](
      val initialNodes: List[Node[E]],
      k: Int,
      r: Float
  ) extends KnnWithinRadius[E](k, r)

  final class KnnWithinSectorImpl[E <: Position](
      val initialNodes: List[Node[E]],
      k: Int,
      sectorDir: Float,
      sectorWidth: Float,
      hitboxSize: Float,
      r: Float
  ) extends KnnWithinSector[E](k, sectorDir, sectorWidth, hitboxSize, r)

}

object KnnSearches {
  trait BaseKnn[E <: Position] extends SearchConfig[E] {
    val k: Int

    def initialState(q: Float2): State[E] = {
      new State[E](
        q,
        FloatMinHeap[Node[E]](0, initialNodes, 4),
        FloatMaxHeapK[E](k),
        new ArrayBuffer[E](k)
      )
    }

    // TODO: Do this with sentinel?
    def maxElemDist(s: State[E]) = if (s.elements.isEmpty) Float.MaxValue else s.elements.maxKey
    def minNodeDist(s: State[E]) = if (s.nodes.isEmpty) Float.MaxValue else s.nodes.minKey
    def elemCloserThanNode(s: State[E]) = minNodeDist(s) >= maxElemDist(s)

    override def collectFoundOrDone(s: State[E]): Boolean = {
      val done = s.nodes.isEmpty || (elemCloserThanNode(s) && s.elements.size == k)
      if (done) {
        s.elements.peekValuesToBuffer(s.foundElements)
      }
      done
    }
  }

  trait Knn[E <: Position]
      extends SearchConfig.NonFiltering[E]
      with IncrementalSearch[E]
      with BaseKnn[E]

  trait KnnWithFilter[E <: Position](val k: Int, filter: E => Boolean)
      extends BaseKnn[E]
      with SearchConfig.DefaultFiltering[E]
      with IncrementalSearch[E] {

    override def collectFoundOrDone(s: State[E]) = super[BaseKnn].collectFoundOrDone(s)

    override def filterElements(e: E, s: State[E]): Boolean = filter(e)
  }

  trait KnnWithinRadius[E <: Position](val k: Int, r: Float)
      extends SearchConfig.DefaultFiltering[E]
      with IncrementalSearch[E]
      with BaseKnn[E] {
    val rSq = r * r
    override def filterElements(e: E, s: State[E]): Boolean =
      e.position.distanceSq(s.queryPoint) <= rSq
    override def filterNodes(n: Node[E], s: State[E]): Boolean =
      n.bounds.distanceSq(s.queryPoint) <= rSq
  }

  trait KnnWithinSector[E <: Position](
      val k: Int,
      sectorDir: Float,
      sectorWidth: Float,
      hitboxSize: Float,
      r: Float
  ) extends SearchConfig.DefaultFiltering[E]
      with IncrementalSearch[E]
      with BaseKnn[E] {
    val rSq = r * r
    val halfSectorMinusHalfPi = sectorWidth / 2 - com.scilari.math.FloatMath.HalfPi

    private[this] val normal1 = Float2.directed(sectorDir + halfSectorMinusHalfPi)
    private[this] val normal2 = Float2.directed(sectorDir - halfSectorMinusHalfPi)
    private[this] val hitbox1 = normal1 * hitboxSize
    private[this] val hitbox2 = normal2 * hitboxSize
    private[this] def insideSector(p: Float2): Boolean =
      normal1.dot(p + hitbox1) > 0f && normal2.dot(p + hitbox2) > 0f

    override def filterElements(e: E, s: State[E]): Boolean = {
      e.position.distanceSq(s.queryPoint) <= rSq && insideSector(e.position - s.queryPoint)
    }

    override def filterNodes(n: Node[E], s: State[E]): Boolean = {
      n.bounds.distanceSq(s.queryPoint) <= rSq && {
        val corners = n.bounds.corners.map { c => c - s.queryPoint }
        !(corners.forall(c => c.dot(normal1) < 0) || corners.forall(c => c.dot(normal2) < 0))
      }
    }
  }

}
