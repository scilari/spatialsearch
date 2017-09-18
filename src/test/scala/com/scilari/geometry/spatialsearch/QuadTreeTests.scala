package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * Created by iv on 1/17/2017.
  */
class QuadTreeTests extends FlatSpec{
  val pointCount = 100
  val points: Seq[Float2] = Seq.fill(pointCount)(Float2.random)
  val queryPoints: Seq[Float2] = Seq.fill(200000)(Float2.random())

  "QuadTree" should "be nonEmpty after adding points" in {
    val quadTree = QuadTree[Float2](AABB.unit)
    assert(quadTree.isEmpty)
    points.foreach{quadTree.add}
    assert(quadTree.nonEmpty)
  }

  val quadTree: QuadTree[Float2] = QuadTree[Float2](AABB.unit)
  points.foreach(quadTree.add)


  it should "find k neighbors" in {
    val k = pointCount / 10
    for(queryPoint <- queryPoints) {
      val knnPoints = quadTree.knnSearch(queryPoint, k)
      knnPoints should have size k
    }
    //println(knnPoints.mkString("\n"))
  }

  it should "have performant knn search" in {
    val k = pointCount / 10
    for(queryPoint <- queryPoints) {
      val knnPoints = quadTree.knnSearch(queryPoint, k)
    }
  }

  it should "have performant range search" in {
    val r = 0.2f
    for(queryPoint <- queryPoints) {
      val knnPoints = quadTree.rangeSearch(queryPoint, r)
    }
  }

  it should "find all and no additional neighbors" in {
    for(queryPoint <- queryPoints.take(10)) {
      val k = pointCount + 2
      quadTree.knnSearch(queryPoint, k) should have size pointCount
    }
  }

  it should "find neighbors within a range" in {
    val range = 0.2f
    for(queryPoint <- queryPoints) {
      val pointsInRange = quadTree.rangeSearch(queryPoint, range)
      val filteredPoints = points.filter(queryPoint.distance(_) <= range)
      pointsInRange should have size filteredPoints.size
    }
  }

  it should "find all neighbors when radius is infinite" in {
    for(queryPoint <- queryPoints.take(10)) {
      quadTree.rangeSearch(queryPoint, Float.PositiveInfinity) should have size points.size
    }
  }

  it should "find only bottom-left corner points" in {
    val k = pointCount / 10
    for(queryPoint <- queryPoints) {
      val cornerPoints =
        quadTree.knnSearchWithCondition(queryPoint, k, (p: Float2) => p.x < 0.5f && p.y < 0.5f)

      cornerPoints should have size k
      for(cp <- cornerPoints){
        assert(cp.x < 0.5f && cp.y < 0.5f)
      }
    }
  }

  it should "find polygonal neighbors" in {
    var neighborSum = 0
    for(queryPoint <- queryPoints) {
      val points = quadTree.polygonalSearch(queryPoint)
      neighborSum += points.size
    }

    val avg = neighborSum.toDouble/queryPoints.size
    println("Average number of neighbors: " + avg)
    assert(avg > 3 && avg < 6)
  }



  import TestResources._
  val cityTree = QuadTree(cityData)

  it should "find the five nearest cities to WGS 65.0 25.0 (ENU origo there)" in {
    val cities = cityTree.knnSearch(Float2(0, 0), 5)
    val names = cities.map{_.data.name}
    names should contain theSameElementsAs List("Oulu", "Kempele", "Muhos", "Raahe", "Haukipudas")
  }

  it should "find the five nearest cities with population over 50,000" in {
    val cities = cityTree.knnSearchWithCondition(
      Float2(0,0), 5, (p: DataPoint[City]) => p.data.population > 50000)

    val names = cities.map{_.data.name}
    names should contain theSameElementsAs List("Oulu", "Vaasa", "Kuopio", "Jyvaskyla", "Joensuu")

  }

  it should "find the five nearest cities with population less than 50,000" in {
    val cities = cityTree.knnSearchWithCondition(
      Float2(0,0), 5, (p: DataPoint[City]) => p.data.population < 50000)

    val names = cities.map{_.data.name}
    names should contain theSameElementsAs List("Oulainen", "Kempele", "Muhos", "Raahe", "Haukipudas")

  }

  it should "find polygonal neighborhood around ENU origo" in {
    val cities = cityTree.polygonalSearch(Float2(0,0))

    val names = cities.map{_.data.name}
    println("Polygonal neighborhood: " + names.mkString(", "))

  }

  it should "contain all elements" in {
    cityTree.size should be (cityData.size)
  }

  it should "contain large and small cities" in {
    val L = cityTree.count(_.data.population > 50000)
    val S = cityTree.count(_.data.population <= 50000)
    (L + S) should be (cityData.size)
    L should be (14)
  }



}
