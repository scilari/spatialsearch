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

  // TODO: add range etc. here
}

