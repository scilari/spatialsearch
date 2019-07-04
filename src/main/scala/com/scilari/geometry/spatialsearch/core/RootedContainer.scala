package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.spatialsearch.SpatialContainer
import com.scilari.geometry.spatialsearch.core.Tree.Node

trait RootedContainer[E, NodeType <: Node[E, NodeType]] extends SpatialContainer[E] {
  var root: NodeType

  /**
    * Adds element to the container.
    */
  override def add(e: E): Unit = {
    root = root.add(e)
  }

  /**
    * Removes element from the container.
    */
  override def remove(e: E): Unit = root.remove(e)

  /**
    * Removes elements satisfying the filter
    */
  override def remove(filter: E => Boolean): Unit = elements.filter(filter).foreach(remove)

  /**
    * Returns the elements in the container
    *
    * @return Elements
    */
  override def elements: Seq[E] = root.elements

  override def nonEmptyIfNotEmptied: Boolean = {
    root.nonLeaf || root.elements.nonEmpty
  }
}
