package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.Float2
import scala.collection.mutable.ArrayBuffer
import com.scilari.geometry.models.Position
import com.scilari.geometry.spatialsearch.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.quadtree.Tree.Node

trait SearchableContainer[E <: Position] {
  protected def initialNodes: List[Node[E]]

  private type Result = ArrayBuffer[E]

  def knnSearch(queryPoint: Float2, k: Int): Result

  def nearest(queryPoint: Float2): Option[E] = knnSearch(queryPoint, 1).headOption

  def nearestAndDist(queryPoint: Float2): Option[(E, Double)] = nearest(queryPoint).map { n =>
    (n, queryPoint.distance(n.position))
  }

  def knnSearchWithFilter(queryPoint: Float2, k: Int, filter: E => Boolean): Result

  def knnWithinRadius(queryPoint: Float2, k: Int, r: Float): Result

  def rangeSearch(queryPoint: Float2, r: Float): Result

  def rangeExcludeNode(queryPoint: Float2, r: Float, node: Node[E]): Result

  def rangeSearchLeaves(queryPoint: Float2, r: Float): List[Node[E]]

  // TODO: find a place for these, as the do not necessarily work with Manhattan
  // def polygonalSearch(queryPoint: Float2): Result

  // def fastPolygonalSearch(queryPoint: Float2): Result

  def isEmptyRange(queryPoint: Float2, r: Float): Boolean

}
