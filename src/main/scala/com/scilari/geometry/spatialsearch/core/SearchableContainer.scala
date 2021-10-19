package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.Float2

trait SearchableContainer[E] {

  def knnSearch(queryPoint: Float2, k: Int): collection.Seq[E]

  def knnSearchWithFilter(queryPoint: Float2, k: Int, filter: E => Boolean): collection.Seq[E]

  def knnWithinRadius(queryPoint: Float2, k: Int, r: Float): collection.Seq[E]

  def rangeSearch(queryPoint: Float2, r: Float): collection.Seq[E]

  // def rangeExcludeNode(queryPoint: Float2, r: Float, node: Node[E]): collection.Seq[E]

  // def rangeSearchLeaves(queryPoint: Float2, r: Float): collection.Seq[Node[E]]

  def polygonalSearch(queryPoint: Float2): collection.Seq[E]

  def fastPolygonalSearch(queryPoint: Float2): collection.Seq[E]

  def isEmptyRange(queryPoint: Float2, r: Float): Boolean

}
