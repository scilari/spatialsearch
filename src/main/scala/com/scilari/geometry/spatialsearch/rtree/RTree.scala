package com.scilari.geometry.spatialsearch.rtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.SearchTree


final class RTree[E <: Float2] private (bb: AABB, nodeElementCapacity: Int)
  extends SearchTree[E] with RTreeLike[E]{

  import Tree._

  var root: BaseType = new LeafType(bb, null, nodeElementCapacity)

  override def add(elems: Seq[E]): Unit = root = root.add(elems)

  def addEnclose(e: E): Unit = add(e) // elements are enclosed

}


object RTree{
  val defaultNodeElementCapacity: Int = 63

  def apply[E <: Float2](bb: AABB, nodeElementCapacity: Int): RTree[E] =
    new RTree[E](bb, nodeElementCapacity)

  def apply[E <: Float2](bb: AABB = AABB.unit): RTree[E] = apply(bb, defaultNodeElementCapacity)

  def apply[E <: Float2](elems: Seq[E], nodeElementCapacity: Int): RTree[E] = {
    require(elems.size > 1, "At least two elements required for creating the initial node.")
    val square = AABB.EnclosingSquare(elems)
    require(square.width > 0 && square.height > 0,
      "At least two spatially distinct elements required for creating the initial node.")
    val q = RTree[E](square, nodeElementCapacity)
    q.add(elems)
    q
  }

  def apply[E <: Float2](elems: Seq[E]): RTree[E] = apply(elems, defaultNodeElementCapacity)


}
