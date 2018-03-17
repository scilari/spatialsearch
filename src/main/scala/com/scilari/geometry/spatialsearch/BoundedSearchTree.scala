package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait BoundedSearchTree[E <: Float2] extends SearchTree[E] with BoundedPlanarTree[E] {

  var root: BaseType

  def knnSearch(queryPoint: Float2, k: Int): Seq[E] = knn(k)(queryPoint, root)

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E] = {
    range(r)(queryPoint, root)
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] =
    knnWithCondition(k, condition)(queryPoint, root)

  override def polygonalSearch(queryPoint: Float2): Seq[E] = polygonal(queryPoint, root)

  override def fastPolygonalSearch(queryPoint: Float2): Seq[E] = polygonalDynamicMaxRange()(queryPoint, root)

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = rangeUntilFirstFound(r)(queryPoint, root).isEmpty


}


