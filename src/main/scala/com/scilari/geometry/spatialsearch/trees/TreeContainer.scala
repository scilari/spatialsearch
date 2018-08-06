package com.scilari.geometry.spatialsearch.trees

import com.scilari.geometry.spatialsearch.SpatialContainer
import com.scilari.geometry.spatialsearch.core.Tree

trait TreeContainer[E] extends Tree[E] with SpatialContainer[E] {
  var root: NodeType

  def add(e: E): Unit = root = root.add(e)

  def depth: Int = root.depth

  def remove(e: E): Unit = root.remove(e)

  override def isEmpty: Boolean = root.isEmpty

  override def nonEmpty: Boolean = root.nonEmpty

  def nonEmptyIfNotEmptied: Boolean = root.nonEmptyIfNotEmptied

  override def remove(elems: Seq[E]): Unit = elems.foreach{ root.remove }

  override def remove(filter: E => Boolean): Unit =
    for{l <- leaves; e <- l.elements if filter(e)} l.elements -= e

  override def toString(): String = root.toString

  override def elements: Seq[E] = root.elements

  def leaves: Seq[LeafType] = root.leaves

}
