package com.scilari.geometry.spatialsearch

// import com.csdgn.util.KDTree
import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.City
import com.scilari.geometry.spatialsearch.core.SearchableContainer
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import org.scalatest._
import flatspec._
import matchers._


trait TreeTestBase extends AnyFlatSpec with should.Matchers {
  type TreeType = QuadTree[Float2] // <: SearchableContainer[Float2]
  val pointCount = 1000
  val points: Seq[Float2] = Seq.fill(pointCount)(Float2.randomMinusOneToOne)
  val queryPoints: Seq[Float2] = Seq.fill(2000)(Float2.randomMinusOneToOne)
  def createEmptyUnitTree: TreeType
  def createFilledTree: TreeType
  def createCityTree: QuadTree[DataPoint[City]]// SearchableContainer[DataPoint[City]]

  def treeName: String
  
  type CityPoint = DataPoint[City]

  val filledTree: TreeType  = createFilledTree
  val searchableCityTree: QuadTree[CityPoint] = createCityTree

  // val kdTree = new KDTree[Float2](2, 48)

  // points.foreach { p =>
  //   kdTree.add(p.toDoubleArray, p)
  // }

}
