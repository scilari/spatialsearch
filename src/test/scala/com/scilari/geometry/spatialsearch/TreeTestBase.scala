package com.scilari.geometry.spatialsearch

import com.csdgn.util.KDTree
import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources.City
import org.scalatest.{FlatSpec, Matchers}

trait TreeTestBase extends FlatSpec with Matchers {
  type TreeType <: SearchableContainer[Float2]
  val pointCount = 1000
  val points: Seq[Float2] = Seq.fill(pointCount)(Float2.randomMinusOneToOne)
  val queryPoints: Seq[Float2] = Seq.fill(2000)(Float2.randomMinusOneToOne)
  def createEmptyUnitTree: TreeType
  def createFilledTree: TreeType
  def createCityTree: SearchableContainer[DataPoint[City]]

  def treeName: String

  val filledTree: TreeType  = createFilledTree
  val searchableCityTree: SearchableContainer[DataPoint[City]] = createCityTree

  val kdTree = new KDTree[Float2](2, 48)

  points.foreach { p =>
    kdTree.add(p.toDoubleArray, p)
  }

}
