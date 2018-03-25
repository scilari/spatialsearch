package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait Searchable[E] {

  def knnSearch(queryPoint: Float2, k: Int): Seq[E]

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E]

  def polygonalSearch(queryPoint: Float2): Seq[E]

  def fastPolygonalSearch(queryPoint: Float2): Seq[E]

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E]

  def isEmptyRange(queryPoint: Float2, r: Float): Boolean = rangeSearch(queryPoint, r).isEmpty

  def nonEmptyRange(queryPoint: Float2, r: Float): Boolean = !isEmptyRange(queryPoint, r)

  def isEmpty: Boolean

  def nonEmpty: Boolean = !isEmpty

  def elements: Seq[E] = rangeSearch(Float2.zero, Float.PositiveInfinity)

  def nearestNeighborSearch(queryPoint: Float2): Option[E] = knnSearch(queryPoint, 1).headOption

}
