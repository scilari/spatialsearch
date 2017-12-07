package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch._

/**
  * Concrete QuadTree implementation of SearchTree
  * @param bb Initial bounding box describing the root
  * @tparam E Element type
  */
final class QuadTree[E <: Float2] private (bb: AABB, parameters: Parameters = Parameters())
  extends SearchTree[E] with QuadTreeLike[E]{

  import Tree.{BaseType, NodeType, LeafType}

  var root: BaseType = new LeafType(bb, null, parameters)

  def addEnclose(e: E): Unit = {
    if(root.contains(e)) {
      add(e)
    } else{
      val newAABB = QuadTreeUtils.expandAABB(e, root)
      val newRoot = new NodeType(newAABB, parameters = parameters)
      newRoot.setChild(QuadTreeUtils.findQuadrant(root.center, newRoot), root)
      root = newRoot
      addEnclose(e)
    }
  }

}

object QuadTree{

  def apply[T <: Float2](bb: AABB, parameters: Parameters): QuadTree[T] = new QuadTree[T](bb, parameters)
  def apply[T <: Float2](bb: AABB = AABB.unit): QuadTree[T] = apply(bb, Parameters())

  def apply[T <: Float2](elems: Seq[T]): QuadTree[T] = apply(elems, Parameters())

  def apply[T <: Float2](elems: Seq[T], parameters: Parameters): QuadTree[T] = {
    require(elems.size > 1, "At least two elements required for creating the initial node.")
    val square = AABB.enclosingSquare(elems)
    require(square.area > 0,
      "At least two spatially distinct elements required for creating the initial node.")
    val q = QuadTree[T](square, parameters)
    elems.foreach(q.add)
    q
  }


}
