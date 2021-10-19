package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.{Rooted, SearchableContainer}
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig.Euclidean
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Node
import com.scilari.geometry.spatialsearch.searches.base.{KnnSearches, Radius}

trait EuclideanSearches[E <: Position] extends SearchableContainer[E] with Rooted[E] {
  import EuclideanSearches._
  
  def knnSearch(queryPoint: Float2, k: Int): collection.Seq[E] = KnnImpl(root, k).search(queryPoint)

  def knnSearchWithFilter(queryPoint: Float2, k: Int, filter: E => Boolean): collection.Seq[E] = 
    KnnWithFilterImpl[E](root, k, filter).search(queryPoint)
  
  def knnWithinRadius(queryPoint: Float2, k: Int, r: Float): collection.Seq[E] = 
    KnnWithinRadiusImpl(root, k, r).search(queryPoint)
  
  def rangeSearch(queryPoint: Float2, r: Float): collection.Seq[E] = RadiusImpl[E](root, r).search(queryPoint)

  def rangeExcludeNode(queryPoint: Float2, r: Float, node: Node[E]): collection.Seq[E] = 
    RadiusImpl[E](root, r).searchExcludeNode(queryPoint, node)
  
  def rangeSearchLeaves(queryPoint: Float2, r: Float): collection.Seq[Node[E]] = 
    RadiusImpl[E](root, r).searchLeaves(queryPoint)

  def polygonalSearch(queryPoint: Float2): collection.Seq[E] = 
    Polygonal.PolygonalImpl[E](root).search(queryPoint)

  def fastPolygonalSearch(queryPoint: Float2): collection.Seq[E] = 
    Polygonal.PolygonalDynamicMaxRange[E](root, 3).search(queryPoint)
  
  // TODO: optimize if possible
  def isEmptyRange(queryPoint: Float2, r: Float): Boolean = rangeSearch(queryPoint, r).isEmpty

}

object EuclideanSearches {
  import KnnSearches._
  
  final class RadiusImpl[E <: Position](var root: Node[E], r: Float) extends Radius[E](r) with Euclidean[E]
  
  final class KnnImpl[E <: Position](var root: Node[E], val k: Int) extends Knn[E] with Euclidean[E]
  
  final class KnnWithFilterImpl[E <: Position](var root: Node[E], k: Int, filter: E => Boolean)
    extends KnnWithFilter[E](k, filter) with Euclidean[E]
  
  final class KnnWithinRadiusImpl[E <: Position](var root: Node[E], k: Int, r: Float) 
    extends KnnWithinRadius[E](k, r) with Euclidean[E]
  
}
