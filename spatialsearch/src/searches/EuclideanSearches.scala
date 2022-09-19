package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.{SearchableContainer}
import com.scilari.geometry.spatialsearch.searches.base.{KnnSearches, RadiusSearches}
import scala.collection.mutable.ArrayBuffer

import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig.euclidean

abstract class EuclideanSearches[E <: Position]
    extends SearchableContainer[E],
      RadiusSearches[E],
      KnnSearches[E] {

  def polygonalSearch(queryPoint: Float2): ArrayBuffer[E] =
    Polygonal.PolygonalImpl[E](initialNodes).search(queryPoint)

  def fastPolygonalSearch(queryPoint: Float2): ArrayBuffer[E] =
    Polygonal.PolygonalDynamicMaxRange[E](initialNodes, maxRangeFactor = 3).search(queryPoint)

}
