package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}

trait BoundedSearchTree[E <: Float2] extends SearchTree[E] {
  override type BaseType <: BoundedBase

  abstract class BoundedBase(bb: AABB) extends AABB(bb) with Base {
    override def contains(p: Float2): Boolean = super[AABB].contains(p)

    override def toString(): String = "Bounded Tree: " + super[AABB].toString()
  }
}




