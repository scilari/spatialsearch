package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait SpatialContainer[E]{
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
    * Removes element from the container by using the element coordinates.
    */
  def remove(elementCoordinates: Float2, e: E)
}
