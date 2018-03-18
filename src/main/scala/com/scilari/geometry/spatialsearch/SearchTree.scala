package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.searches.Searches

/**
  * Ties the searches and operations to a root node
  * @tparam E Element type
  */
trait SearchTree[E <: Float2] extends Tree[Float2, E] with Searchable[E] with SpatialContainer[E]
  with Searches[Float2, E] with Traversable[E]{

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


  def add(e: E): Unit = root = root.add(e)

  def depth: Int = root.depth

  def remove(e: E): Unit = remove(e, e)

  def remove(queryPoint: Float2, e: E): Unit = removal(e: E)(queryPoint, root)

  override def isEmpty: Boolean = root.isEmpty
  override def nonEmpty: Boolean = root.nonEmpty

  override def remove(elems: Seq[E]): Unit = for(l <- leaves) l.elements --= elems.filter(l.contains)

  override def remove(filter: E => Boolean): Unit =
    for{l <- leaves; e <- l.elements if filter(e)} l.elements -= e

  def foreach[U](f: E => U): Unit = root.foreach(f)

  override def toString(): String = root.toString()

  override def elements: Seq[E] = root.elements

  def leaves: Seq[LeafType] = root.leaves

}
