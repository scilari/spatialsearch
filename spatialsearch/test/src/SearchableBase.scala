package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.City
import com.scilari.geometry.spatialsearch.core.SearchableContainer
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.searches.euclidean.{EuclideanSearches, SeqSearches}
import org.scalatest._
import flatspec._
import matchers._

trait SearchableBase extends TreeTestBase {
  
  type CityTreeType = EuclideanSearches[CityPoint] with SeqSearches[CityPoint]
  type PointTreeType = EuclideanSearches[Float2] with SeqSearches[Float2]
  
  def createPointTree: PointTreeType
  def createCityTree: CityTreeType

  val filledTree: PointTreeType  = createPointTree
  val searchableCityTree: CityTreeType = createCityTree
  
}
