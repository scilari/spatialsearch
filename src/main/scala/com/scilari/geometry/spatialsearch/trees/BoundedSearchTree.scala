package com.scilari.geometry.spatialsearch.trees

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.searches.SearchesImpl

trait BoundedSearchTree[E <: Float2] extends SearchesImpl[E] with TreeContainer[E] {
  override type NodeType <: BoundedNode

  abstract class BoundedNode(bb: AABB) extends AABB(bb) with Node {
    this: NodeType =>
    override def encloses(e: E): Boolean = super[AABB].contains(e)
    override def toString: String = "Bounded Tree: " + super[AABB].toString()
  }
}




