package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.Float2

trait EuclideanSearches[EE <: Float2] extends EuclideanTypes[EE] {
  def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    val knn = new KnnImpl(root, k)
    knn.search(queryPoint)
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E] = {
    val radius = new RadiusImpl[EE](root, r)
    radius.search(queryPoint)
  }

  def rangeSearchLeaves[Q <: Float2](queryPoint: Q, r: Float): Seq[NodeType] = {
    val radius = new RadiusImpl[EE](root, r)
    radius.searchLeaves(queryPoint)
  }

  def polygonalSearch(queryPoint: Float2): Seq[E] = {
    val polygonal = new Polygonal.PolygonalImpl[E](root)
    polygonal.search(queryPoint)
  }

  def fastPolygonalSearch(queryPoint: Float2): Seq[E] = {
    val polygonal = new Polygonal.PolygonalDynamicMaxRange[E](root, 3)
    polygonal.search(queryPoint)
  }

}
