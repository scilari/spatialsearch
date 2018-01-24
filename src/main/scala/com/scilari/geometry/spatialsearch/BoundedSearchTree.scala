package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait BoundedSearchTree[E <: Float2] extends SearchTree[E] {

  val Tree: BoundedPlanarTree[E]
  var root: Tree.BaseType

  def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    new Knn(k).search(queryPoint, root)
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E] =
    rangeSearch(queryPoint, r, sizeHint = Searches.defaultRangeSizeHint)

  def rangeSearch(queryPoint: Float2, r: Float, sizeHint: Int = Searches.defaultRangeSizeHint): Seq[E] = {
    new Range(r, sizeHint).search(queryPoint, root)
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    new KnnWithCondition(k, condition).search(queryPoint, root)
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    new Polygonal().search(queryPoint, root)
  }

  override def fastPolygonalSearch(queryPoint: Float2): Seq[E] = polygonalDynamicMaxRangeSearch(queryPoint)

  def polygonalMaxRangeSearch(queryPoint: Float2, maxRange: Float): Seq[E] = {
    new PolygonalMaxRange(maxRange).search(queryPoint, root)
  }

  def polygonalDynamicMaxRangeSearch(queryPoint: Float2, maxRangeFactor: Float = 3f): Seq[E] = {
    new PolygonalDynamicMaxRange(maxRangeFactor).search(queryPoint, root)
  }

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = {
    new RangeUntilFirstFound(r).search(queryPoint, root).isEmpty
  }


}


