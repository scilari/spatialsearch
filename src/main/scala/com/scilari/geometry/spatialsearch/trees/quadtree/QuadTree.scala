package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.SearchTree

/**
  * Concrete QuadTree implementation of SearchTree
  * @param bb Initial bounding box describing the root
  * @tparam E Element type
  */
final class QuadTree[E <: Float2] private (bb: AABB, val parameters: Parameters)
  extends QuadTreeLike[E] with SearchTree[E] {

  var root: NodeType = new LeafType(bb, None, parameters)

  def addEnclose(e: E): Unit = {
    if(root.encloses(e)) {
      add(e)
    } else{
      val newAABB = QuadTreeUtils.expandAABB(e, root)
      val newRoot = new BranchType(newAABB, None, parameters)
      newRoot.setChild(QuadTreeUtils.findQuadrant(root.center, newRoot), root)
      root = newRoot
      addEnclose(e)
    }
  }

}

object QuadTree{

  def apply[E <: Float2](bb: AABB, parameters: Parameters): SearchTree[E] = new QuadTree[E](bb, parameters)
  def apply[E <: Float2](bb: AABB = AABB.unit): SearchTree[E] = apply(bb, Parameters(bb))

  def apply[E <: Float2](elems: Seq[E]): SearchTree[E] = apply(elems, Parameters())

  def apply[E <: Float2](elems: Seq[E], parameters: Parameters): SearchTree[E] = {
    val square = AABB.EnclosingSquare(elems)
    require(square.area > 0,
      "At least two spatially distinct elements required for creating the initial node.")

    // if default, use params depending on root node size
    val p = if(parameters == Parameters()) Parameters(square) else parameters
    val q = QuadTree[E](square, p)
    q.add(elems)
    q
  }

  implicit def SearchTreeToQuadTree[E <: Float2](t: SearchTree[E]): QuadTree[E] = t.asInstanceOf[QuadTree[E]]

}
