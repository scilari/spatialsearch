package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.Float2

/**
  * Ties searches to a tree root
  */
object TreeSearches{
  trait Base[P, E <: Float2] extends Searches[P, E] {
    var root: BaseType
    // TODO: this is a horrible cast
    private[TreeSearches] def castRoot =
      root.asInstanceOf[basicSearches.BaseType with polygonalSearches.BaseType with seqSearches.BaseType]
  }

  trait Basic[P,  E <: Float2] extends Base[P, E] {
    def knnSearch(queryPoint: P, k: Int): Seq[E] = basicSearches.knn(k)(queryPoint, castRoot)

    def rangeSearch(queryPoint: P, r: Float): Seq[E] = basicSearches.range(r)(queryPoint, castRoot)

    def knnSearchWithCondition(queryPoint: P, k: Int, condition: E => Boolean): Seq[E] =
      basicSearches.knnWithCondition(k, condition)(queryPoint, castRoot)

    def isEmptyRange(queryPoint: P, r: Float): Boolean = basicSearches.rangeUntilFirstFound(r)(queryPoint, castRoot).isEmpty
  }

  trait Polygonal[P <: Float2, E <: Float2] extends Base[P, E]{
    def polygonalSearch(queryPoint: P): Seq[E] = polygonalSearches.polygonal(queryPoint, castRoot)

    def fastPolygonalSearch(queryPoint: P): Seq[E] = polygonalSearches.polygonalDynamicMaxRange()(queryPoint, castRoot)
  }

  trait Sequence[P <: Float2, E <: Float2] extends Base[P, E]{
    def seqRangeSearch(queryPoints: IndexedSeq[P], r: Float): Seq[E] = {
      if(queryPoints.nonEmpty) seqSearches.range(r)(queryPoints, castRoot) else Seq.empty[E]
    }

    def seqKnnSearch(queryPoints: IndexedSeq[P], k: Int): Seq[E] = {
      if(queryPoints.nonEmpty) seqSearches.knn(k)(queryPoints, castRoot) else Seq.empty[E]
    }
  }

  trait Combined[P <: Float2, E <: Float2] extends Basic[P, E] with Polygonal[P, E] with Sequence[P, E]

}



