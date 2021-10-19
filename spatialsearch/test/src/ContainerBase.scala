package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.City
import com.scilari.geometry.spatialsearch.core.SearchableContainer
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.searches.euclidean.{EuclideanSearches, SeqSearches}
import org.scalatest._
import flatspec._
import matchers._

trait ContainerBase extends TreeTestBase {
    type CityContainerType = QuadTree[CityPoint]
    type PointContainerType = QuadTree[Float2]
  
    def createPointContainer: PointContainerType
    def createCityContainer: CityContainerType

    val filledContainer: PointContainerType  = createPointContainer
    val searchableCityContainer: CityContainerType = createCityContainer

    def createEmptyUnitContainer: QuadTree[Float2]


}
