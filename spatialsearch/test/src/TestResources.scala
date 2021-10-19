package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestUtils._

object TestResources {
  case class City(name: String, population: Int)
  val rawCityData = readCsvRows("./spatialsearch/test/resources/finnish_cities_enu_km_y2000.csv")
  val cityData = rawCityData.map{ d =>
    val name = d(0)
    val pop = d(1).toInt
    val x = d(2).toFloat
    val y = d(3).toFloat

    new DataPoint[City](Float2(x, y), City(name, pop))
  }

  val cityAABB = AABB(cityData.map{_.position})

}
