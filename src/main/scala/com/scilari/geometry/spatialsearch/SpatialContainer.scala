package com.scilari.geometry.spatialsearch

trait SpatialContainer[E] extends Traversable[E]{
  /**
    * Adds element to the container.
    */
  def add(e: E): Unit

  /**
    * Adds multiple elements to the container.
    */
  def add(es: Seq[E]): Unit = es.foreach(add)

  /**
    * Adds element to the container and adjusts the spatial bounds to that the element
    * is enclosed inside.
    */
  def addEnclose(e: E): Unit

  /**
    * Removes element from the container.
    */
  def remove(e: E): Unit

  /**
    * Removes multiple elements from the container.
    */
  def remove(elems: Seq[E]): Unit = elems.foreach(remove)

  /**
    * Removes elements satisfying the filter
    */
  def remove(filter: E => Boolean): Unit

  /**
    * Returns the elements in the container
    * @return Elements
    */
  def elements: Seq[E]

  def foreach[U](f: E => U): Unit = elements.foreach(f)

  def isEmpty: Boolean

  override def nonEmpty: Boolean = !isEmpty
}
