package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.Position
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Branch
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTreeUtils}

trait RootedContainer[E <: Position] extends Rooted[E] {
  val parameters: Parameters
  
  /**
   * Adds element to the container.
   */
  def add(e: E): Unit = {
    root = root.add(e)
  }

  /**
   * Adds elements to the container.
   */
  def add(es: collection.Seq[E]): Unit = es.foreach(add)

  /**
   * Removes element from the container.
   */
  def remove(e: E): Unit = root.remove(e)
  
  def remove(es: collection.Seq[E]): Unit = es.foreach(remove)

  /**
   * Removes elements satisfying the filter
   */
  def remove(filter: E => Boolean): Unit = elements.filter(filter).foreach(remove)

  /**
   * Returns the elements in the container
   *
   * @return Elements
   */
  def elements: collection.Seq[E] = root.elements
  
  def size: Int = root.size
  
  def isEmpty: Boolean = elements.isEmpty
  
  def nonEmpty: Boolean = !isEmpty
  
  // TODO: foreach
  
  def nonEmptyIfNotEmptied: Boolean = {
    root.nonLeaf || root.elements.nonEmpty
  }

  def addEnclose(e: E): Unit = {
    if(root.encloses(e)) {
      add(e)
    } else{
      val newAABB = QuadTreeUtils.expandedAABB(e.position, root.bounds)
      val newRoot = new Branch[E](newAABB, None, parameters)
      newRoot.setChild(QuadTreeUtils.findQuadrant(root.bounds.center, newRoot.bounds), root)
      root = newRoot
      addEnclose(e)
    }
  }

}
