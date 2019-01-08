package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2


trait Searchable[E] {

  def knnSearch(queryPoint: Float2, k: Int): Seq[E]

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E]

  def polygonalSearch(queryPoint: Float2): Seq[E]

  def fastPolygonalSearch(queryPoint: Float2): Seq[E]

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E]

  def seqKnnSearch(queryPoints: IndexedSeq[Float2], k: Int): Seq[E]

  def seqRangeSearch(queryPoints: IndexedSeq[Float2], r: Float): Seq[E]

  def isEmptyRange(queryPoint: Float2, r: Float): Boolean

  def nonEmptyRange(queryPoint: Float2, r: Float): Boolean = !isEmptyRange(queryPoint, r)

  def nearestNeighborSearch(queryPoint: Float2): Option[E] = knnSearch(queryPoint, 1).headOption

}
