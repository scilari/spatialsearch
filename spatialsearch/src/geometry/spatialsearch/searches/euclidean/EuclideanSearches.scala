package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.{Rooted, SearchableContainer}
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig.Euclidean
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Node
import com.scilari.geometry.spatialsearch.searches.base.{KnnSearches, Radius}
import scala.collection.mutable.ArrayBuffer

trait EuclideanSearches[E <: Position] extends SearchableContainer[E] with Rooted[E] {
  import EuclideanSearches._

  def knnSearch(queryPoint: Float2, k: Int): ArrayBuffer[E] = KnnImpl(root, k).search(queryPoint)

  def knnSearchWithFilter(queryPoint: Float2, k: Int, filter: E => Boolean): ArrayBuffer[E] =
    KnnWithFilterImpl[E](root, k, filter).search(queryPoint)

  def knnWithinRadius(queryPoint: Float2, k: Int, r: Float): ArrayBuffer[E] =
    KnnWithinRadiusImpl(root, k, r).search(queryPoint)

  def knnWithinSector(
      queryPoint: Float2,
      k: Int,
      sectorDir: Float,
      sectorWidth: Float,
      r: Float = Float.PositiveInfinity
  ): ArrayBuffer[E] =
    KnnWithinSectorImpl(root, k, sectorDir, sectorWidth, 0f, r).search(queryPoint)

  def beamSearch(
      queryPoint: Float2,
      dir: Float,
      beamWidth: Float,
      beamLength: Float = Float.PositiveInfinity,
      k: Int = 1
  ): ArrayBuffer[E] =
    KnnWithinSectorImpl(
      root,
      k = k,
      sectorDir = dir,
      sectorWidth = 0f,
      hitboxSize = beamWidth,
      r = beamLength
    ).search(queryPoint)

  def rangeSearch(queryPoint: Float2, r: Float): ArrayBuffer[E] =
    RadiusImpl[E](root, r).search(queryPoint)

  def rangeExcludeNode(queryPoint: Float2, r: Float, node: Node[E]): ArrayBuffer[E] =
    RadiusImpl[E](root, r).searchExcludeNode(queryPoint, node)

  def rangeSearchLeaves(queryPoint: Float2, r: Float): List[Node[E]] =
    RadiusImpl[E](root, r).searchLeaves(queryPoint)

  def polygonalSearch(queryPoint: Float2): ArrayBuffer[E] =
    Polygonal.PolygonalImpl[E](root).search(queryPoint)

  def fastPolygonalSearch(queryPoint: Float2): ArrayBuffer[E] =
    Polygonal.PolygonalDynamicMaxRange[E](root, 3).search(queryPoint)

  // TODO: optimize if possible
  def isEmptyRange(queryPoint: Float2, r: Float): Boolean = rangeSearch(queryPoint, r).isEmpty

}

object EuclideanSearches {
  import KnnSearches._

  final class RadiusImpl[E <: Position](var root: Node[E], r: Float)
      extends Radius[E](r)
      with Euclidean[E]

  final class KnnImpl[E <: Position](var root: Node[E], val k: Int) extends Knn[E] with Euclidean[E]

  final class KnnWithFilterImpl[E <: Position](var root: Node[E], k: Int, filter: E => Boolean)
      extends KnnWithFilter[E](k, filter)
      with Euclidean[E]

  final class KnnWithinRadiusImpl[E <: Position](var root: Node[E], k: Int, r: Float)
      extends KnnWithinRadius[E](k, r)
      with Euclidean[E]

  final class KnnWithinSectorImpl[E <: Position](
      var root: Node[E],
      k: Int,
      sectorDir: Float,
      sectorWidth: Float,
      hitboxSize: Float,
      r: Float
  ) extends KnnWithinSector[E](k, sectorDir, sectorWidth, hitboxSize, r)
      with Euclidean[E]

}
