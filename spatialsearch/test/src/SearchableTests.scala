package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{Float2, DataPoint}
import com.scilari.math.FloatMath

abstract class SearchableTests extends SearchableBase {
  treeName should "find k neighbors" in {
    val k = pointCount / 10
    for (queryPoint <- queryPoints) {
      val knnPoints = filledTree.knnSearch(queryPoint, k)
      knnPoints should have size k
    }
  }

  it should "have working knn search" in {
    val k = pointCount / 10
    for (queryPoint <- queryPoints) {
      val knnPoints = filledTree.knnSearch(queryPoint, k)
      knnPoints should have size k
    }
  }

  it should "find all and no additional neighbors" in {
    for (queryPoint <- queryPoints.take(10)) {
      val k = pointCount + 2
      filledTree.knnSearch(queryPoint, k) should have size pointCount
    }
  }

  it should "have working range search" in {
    val r = 0.2f
    for (queryPoint <- queryPoints) {
      val rangePoints = filledTree.rangeSearch(queryPoint, r)
      assert(rangePoints.forall { p => p.distance(queryPoint) <= r })
    }
  }

  it should "find neighbors within a range" in {
    val range = 0.2f
    for (queryPoint <- queryPoints) {
      val pointsInRange = filledTree.rangeSearch(queryPoint, range)
      val filteredPoints = points.filter(queryPoint.distance(_) <= range)
      pointsInRange should have size filteredPoints.size
    }
  }

  // it should "find same neighbors as KdTree inside range" in {
  //   val range = 0.2f
  //   for(queryPoint <- queryPoints) {
  //     val pointsInRange = filledTree.rangeSearch(queryPoint, range)
  //     val q = queryPoint.toDoubleArray
  //     val kdRangeLow = Array(q(0) - range, q(1) - range)
  //     val kdRangeHigh = Array(q(0) + range, q(1) + range)
  //     import collection.JavaConverters._
  //     val kdPointsInRange = kdTree.getRange(kdRangeLow, kdRangeHigh).asScala
  //     val filteredPoints = kdPointsInRange.filter(p => p.distance(queryPoint) <= range)
  //     pointsInRange should have size filteredPoints.size
  //   }
  // }

  it should "find all neighbors when radius is infinite" in {
    for (queryPoint <- queryPoints.take(10)) {
      filledTree.rangeSearch(queryPoint, Float.MaxValue) should have size points.size
    }
  }

  it should "find only bottom-left corner points" in {
    val k = pointCount / 10
    for (queryPoint <- queryPoints) {
      val cornerPoints =
        filledTree.knnSearchWithFilter(queryPoint, k, (p: Float2) => p.x < 0.5f && p.y < 0.5f)

      cornerPoints should have size k
      for (cp <- cornerPoints) {
        assert(cp.x < 0.5f && cp.y < 0.5f)
      }
    }
  }

  it should "find polygonal neighbors" in {
    var neighborSum = 0
    for (queryPoint <- queryPoints) {
      val points = filledTree.polygonalSearch(queryPoint)
      neighborSum += points.size
    }

    val avg = neighborSum.toDouble / queryPoints.size
    assert(avg > 3 && avg < 6)
  }

  it should "find the five nearest cities to WGS 65.0 25.0 (ENU origo there)" in {
    val cities = searchableCityTree.knnSearch(Float2(0, 0), 5)
    val names = cities.map { _.data.name }
    names should contain theSameElementsAs List("Oulu", "Kempele", "Muhos", "Raahe", "Haukipudas")
  }

  it should "find the five nearest cities with population over 50,000" in {
    val cities = searchableCityTree.knnSearchWithFilter(
      Float2(0, 0),
      5,
      (p: CityPoint) => p.data.population > 50000
    )

    val names = cities.map { _.data.name }
    names should contain theSameElementsAs List("Oulu", "Vaasa", "Kuopio", "Jyvaskyla", "Joensuu")

  }

  it should "find the five nearest cities with population less than 50,000" in {
    val cities = searchableCityTree.knnSearchWithFilter(
      Float2(0, 0),
      5,
      (p: CityPoint) => p.data.population < 50000
    )

    val names = cities.map { _.data.name }
    names should contain theSameElementsAs List(
      "Oulainen",
      "Kempele",
      "Muhos",
      "Raahe",
      "Haukipudas"
    )

  }

  it should "find polygonal neighborhood around ENU origo" in {
    val cities = searchableCityTree.polygonalSearch(Float2(0, 0))
    val names = cities.map { _.data.name }
    names should contain theSameElementsAs List("Oulu", "Haukipudas", "Raahe")
  }

  it should "find k closest cities to a sequence of points" in {
    val queryPoints = TestResources.cityData
      .filter(c => Set("Oulu", "Helsinki").contains(c.data.name))
      .map { c => c.position }
    val cities: collection.Seq[CityPoint] = searchableCityTree.seqKnnSearch(queryPoints.toBuffer, 5)

    val names = cities.map { _.data.name }
    names should contain theSameElementsAs List("Oulu", "Helsinki", "Kempele", "Vantaa", "Espoo")
  }

  it should "find cities within a radius from a sequence of points" in {
    val queryPoints = TestResources.cityData
      .filter(c => Set("Oulu", "Helsinki").contains(c.data.name))
      .map { c => c.position }
    val cities: collection.Seq[CityPoint] =
      searchableCityTree.seqRangeSearch(queryPoints.toBuffer, 16.5f)
    val names = cities.map { _.data.name }
    names should contain theSameElementsAs List("Oulu", "Helsinki", "Kempele", "Vantaa", "Espoo")
  }

  it should "find cities within sector from Oulu to Kemi" in {
    import com.scilari.math.FloatMath.Pi
    val ouluPos = TestResources.cityData.find(_.data.name == "Oulu").get.position
    val kemiPos = TestResources.cityData.find(_.data.name == "Kemi").get.position
    val sectorDir = (kemiPos - ouluPos).direction
    val sectorWidth = 0.3f * Pi
    val cities: collection.Seq[CityPoint] =
      searchableCityTree.knnWithinSector(ouluPos, 5, sectorDir, sectorWidth)
    val names = cities.map { _.data.name }
    info("Cities found within sector: " + names.mkString(", "))
    names should contain theSameElementsAs List("Tornio", "Kemi", "Haukipudas")
  }

  it should "find cities within sector from Helsinki to Porvoo" in {
    import com.scilari.math.FloatMath.Pi
    val hkiPos = TestResources.cityData.find(_.data.name == "Helsinki").get.position
    val porvooPos = TestResources.cityData.find(_.data.name == "Porvoo").get.position
    val sectorDir = (porvooPos - hkiPos).direction
    val sectorWidth = 0.5f * Pi
    val cities: collection.Seq[CityPoint] =
      searchableCityTree.knnWithinSector(hkiPos, 5, sectorDir, sectorWidth)
    val names = cities.map { _.data.name }
    info("Cities found within sector: " + names.mkString(", "))
    names should contain theSameElementsAs List("Porvoo", "Sipoo", "Jarvenpaa", "Vantaa", "Kerava")
  }

  it should "find Kemi with beam search" in {
    val ouluPos = TestResources.cityData.find(_.data.name == "Oulu").get.position
    val kemiPos = TestResources.cityData.find(_.data.name == "Kemi").get.position
    val sectorDir = (kemiPos - ouluPos).direction
    val citiesWithBeam0 =
      searchableCityTree.beamSearch(ouluPos + Float2(0.1f), sectorDir, beamWidth = 0f)
    citiesWithBeam0 should have size (0)

    val citiesWithBeam2 =
      searchableCityTree.beamSearch(ouluPos + Float2(0.1f), sectorDir, beamWidth = 0.2f, k = 2)
    val names = citiesWithBeam2.map { _.data.name }
    info("Cities found with Kemi beam: " + names.mkString(", "))
    names should contain theSameElementsAs List("Oulu", "Kemi")
  }

}
