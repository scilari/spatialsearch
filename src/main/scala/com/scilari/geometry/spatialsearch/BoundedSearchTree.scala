package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}

trait BoundedSearchTree[E <: Float2] extends SearchTree[E] with BoundedPlanarTree[E] {

  var root: BaseType

  def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    val s = new Knn(k)
    s.search(queryPoint, root.asInstanceOf[s.BaseType])
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E] =
    rangeSearch(queryPoint, r, sizeHint = Searches.defaultRangeSizeHint)

  def rangeSearch(queryPoint: Float2, r: Float, sizeHint: Int = Searches.defaultRangeSizeHint): Seq[E] = {
    val s = new Range(r, sizeHint)
    s.search(queryPoint, root.asInstanceOf[s.BaseType])
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    val s = new KnnWithCondition(k, condition)
    s.search(queryPoint, root.asInstanceOf[s.BaseType])
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    val s = new Polygonal()
    s.search(queryPoint, root.asInstanceOf[s.BaseType])
  }

  override def fastPolygonalSearch(queryPoint: Float2): Seq[E] = polygonalDynamicMaxRangeSearch(queryPoint)

  def polygonalMaxRangeSearch(queryPoint: Float2, maxRange: Float): Seq[E] = {
    val s = new PolygonalMaxRange(maxRange)
    s.search(queryPoint, root.asInstanceOf[s.BaseType])
  }

  def polygonalDynamicMaxRangeSearch(queryPoint: Float2, maxRangeFactor: Float = 3f): Seq[E] = {
    val s = new PolygonalDynamicMaxRange(maxRangeFactor)
    s.search(queryPoint, root.asInstanceOf[s.BaseType])
  }

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = {
    val s = new RangeUntilFirstFound(r)
    s.search(queryPoint, root.asInstanceOf[s.BaseType]).isEmpty
  }


}


