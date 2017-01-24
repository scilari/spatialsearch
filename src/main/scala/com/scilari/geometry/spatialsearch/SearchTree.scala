package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

/**
  * Created by Ilari.Vallivaara on 1/18/2017.
  */
trait SearchTree[T <: Float2] {

  def knnSearch(queryPoint: Float2, k: Int): Seq[T]

  def rangeSearch(queryPoint: Float2, r: Float): Seq[T]

  def polygonalSearch(queryPoint: Float2): Seq[T]

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: T => Boolean): Seq[T]

  def isEmptyRange(queryPoint: Float2, r: Float):  Boolean = {
    rangeSearch(queryPoint, r).isEmpty
  }

  def nonEmptyRange(queryPoint: Float2, r: Float): Boolean = !isEmptyRange(queryPoint, r)

}
