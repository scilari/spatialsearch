package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.Position
import scala.collection.immutable.ArraySeq
import scala.collection.mutable.ArrayBuffer
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils
import com.scilari.geometry.spatialsearch.quadtree.Tree.{Node, Branch}
import com.scilari.geometry.spatialsearch.quadtree.Parameters

trait RootedContainer[E <: Position] {
  var root: Node[E]

  /** Adds element to the container.
    */
  def add(e: E): Unit = {
    root = root.add(e)
  }

  /** Adds elements to the container.
    */
  def add(es: Iterable[E]): Unit = es.foreach(add)

  /** Removes element from the container.
    */
  def remove(e: E): Unit = root.remove(e)

  def remove(es: Iterable[E]): Unit = es.foreach(remove)

  /** Removes elements satisfying the filter
    */
  def remove(filter: E => Boolean): Unit = elements.filter(filter).foreach(remove)

  /** Returns the elements in the container
    *
    * @return
    *   Elements
    */
  def elements: ArrayBuffer[E] = root.elements

  def size: Int = root.size

  def isEmpty: Boolean = elements.isEmpty

  def nonEmpty: Boolean = !isEmpty

  // TODO: foreach

  def nonEmptyIfNotEmptied: Boolean = {
    root.nonLeaf || root.elements.nonEmpty
  }

  // TODO: These are dependent on QuadTree, so refactor somewhere else
  val parameters: Parameters
  def addEnclose(e: E): Unit = {
    if (root.encloses(e)) {
      add(e)
    } else {
      val newAABB = QuadTreeUtils.expandedAABB(e.position, root.bounds)
      val newRoot = new Branch[E](newAABB, None, parameters)
      newRoot.setChild(
        QuadTreeUtils.Quadrants.findQuadrant(root.bounds.center, newRoot.bounds),
        root
      )
      root = newRoot
      addEnclose(e)
    }
  }

}
