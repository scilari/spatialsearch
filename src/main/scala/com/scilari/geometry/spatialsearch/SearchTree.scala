package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait SearchTree[E <: Float2] extends Searchable[E] with SpatialContainer[E]
  with Searches[Float2, E] with PolygonalSearches[Float2, E] with Traversable[E]{

  val Tree: Tree[Float2, E]
  var root: Tree.BaseType

  def add(e: E): Unit = root = root.add(e)

  def depth: Int = root.depth

  def remove(e: E): Unit = remove(e, e)

  def remove(queryPoint: Float2, e: E): Unit = {
    val removal = new Removal(e)
    removal.search(queryPoint, root)
  }

  override def isEmpty: Boolean = root.isEmpty
  override def nonEmpty: Boolean = root.nonEmpty

  override def remove(elems: Seq[E]): Unit = for(l <- leaves) l.elements --= elems.filter(l.contains)

  override def remove(filter: E => Boolean): Unit =
    for{l <- leaves; e <- l.elements if filter(e)} l.elements -= e

  def foreach[U](f: E => U): Unit = root.foreach(f)

  override def toString(): String = root.toString()

  def elements: Seq[E] = root.elements

  def leaves: Seq[Tree.Leaf] = root.leaves





}
