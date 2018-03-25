package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{DataPoint, Float2}
import TestResources._
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree
import org.csdgn.util.KDTree
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by iv on 1/17/2017.
  */
abstract class TreeTests extends FlatSpec with Matchers {

  val pointCount = 1000
  val points: Seq[Float2] = Seq.fill(pointCount)(Float2.random)
  val queryPoints: Seq[Float2] = Seq.fill(20000)(Float2.random)

  def createEmptyUnitTree: BoundedSearchTree[Float2]
  def createFilledTree: BoundedSearchTree[Float2]
  def createCityTree: BoundedSearchTree[DataPoint[City]]
  def treeName: String


  treeName should "be nonEmpty after adding points" in {
    val tree = createEmptyUnitTree
    assert(tree.isEmpty)
    points.foreach {
      tree.add
    }
    assert(tree.nonEmpty)
  }

  private val tree = createFilledTree
  private val kdTree = new KDTree[Float2](2, 48)

  points.foreach { p =>
    kdTree.add(p.toDoubleArray, p)
  }


  it should "find k neighbors" in {
    val k = pointCount / 10
    for(queryPoint <- queryPoints) {
      val knnPoints = tree.knnSearch(queryPoint, k)
      knnPoints should have size k
    }
  }

  it should "have working knn search" in {
    val k = pointCount / 10
    for(queryPoint <- queryPoints) {
      val knnPoints = tree.knnSearch(queryPoint, k)
      knnPoints should have size k
    }
  }

  it should "have working range search" in {
    val r = 0.2f
    for(queryPoint <- queryPoints) {
      val rangePoints = tree.rangeSearch(queryPoint, r)
      assert(rangePoints.forall{p => p.distance(queryPoint) <= r })
    }
  }

  it should "find all and no additional neighbors" in {
    for(queryPoint <- queryPoints.take(10)) {
      val k = pointCount + 2
      tree.knnSearch(queryPoint, k) should have size pointCount
    }
  }

  it should "find neighbors within a range" in {
    val range = 0.2f
    for(queryPoint <- queryPoints) {
      val pointsInRange = tree.rangeSearch(queryPoint, range)
      val filteredPoints = points.filter(queryPoint.distance(_) <= range)
      pointsInRange should have size filteredPoints.size
    }
  }

  it should "find same neighbors as KdTree inside range" in {
    val range = 0.2f
    for(queryPoint <- queryPoints) {
      val pointsInRange = tree.rangeSearch(queryPoint, range)
      val q = queryPoint.toDoubleArray
      val kdRangeLow = Array(q(0) - range, q(1) - range)
      val kdRangeHigh = Array(q(0) + range, q(1) + range)
      import collection.JavaConverters._
      val kdPointsInRange = kdTree.getRange(kdRangeLow, kdRangeHigh).asScala
      val filteredPoints = kdPointsInRange.filter(p => p.distance(queryPoint) <= range)
      pointsInRange should have size filteredPoints.size
    }
  }

  it should "find all neighbors when radius is infinite" in {
    for(queryPoint <- queryPoints.take(10)) {
      tree.rangeSearch(queryPoint, Float.PositiveInfinity) should have size points.size
    }
  }

  it should "find only bottom-left corner points" in {
    val k = pointCount / 10
    for(queryPoint <- queryPoints) {
      val cornerPoints =
        tree.knnSearchWithCondition(queryPoint, k, (p: Float2) => p.x < 0.5f && p.y < 0.5f)

      cornerPoints should have size k
      for(cp <- cornerPoints){
        assert(cp.x < 0.5f && cp.y < 0.5f)
      }
    }
  }

  it should "find polygonal neighbors" in {
    var neighborSum = 0
    for(queryPoint <- queryPoints) {
      val points = tree.polygonalSearch(queryPoint)
      neighborSum += points.size
    }

    val avg = neighborSum.toDouble/queryPoints.size
    assert(avg > 3 && avg < 6)
  }




  val cityTree: BoundedSearchTree[DataPoint[City]] = createCityTree

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
    names should contain theSameElementsAs List("Oulu", "Haukipudas", "Raahe")

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

  it should "have removal functionality" in {
    val cityTree = createCityTree
    val toBeRemoved = cityData.take(3)
    for(e <- toBeRemoved){
      cityTree.remove(e, e)
    }

    val treeNames = cityTree.toList.map{_.data.name}
    val names = cityData.drop(3).map{_.data.name}
    treeNames should contain theSameElementsAs names

  }

  it should "have simultaneous removal functionality" in {
    val cityTree = createCityTree
    val toBeRemoved = cityData.take(10)
    cityTree.remove(toBeRemoved)
    val treeNames = cityTree.toList.map{_.data.name}
    val names = cityData.drop(10).map{_.data.name}
    treeNames should contain theSameElementsAs names


  }

  it should "have consistent leaf and inner code counts" in {
    val leaves = cityTree.root.leaves
    val nodes = cityTree.root.nodes
    val innerNodes = nodes.filter{n => n.nonLeaf}
    nodes.size should be (leaves.size + innerNodes.size)
  }



}
