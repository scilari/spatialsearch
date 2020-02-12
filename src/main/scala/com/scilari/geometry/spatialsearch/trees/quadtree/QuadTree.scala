package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2, HasPosition}
import com.scilari.geometry.spatialsearch.SearchableContainer
import com.scilari.geometry.spatialsearch.core.RootedContainer
import com.scilari.geometry.spatialsearch.searches.euclidean.{EuclideanSearches, RadiusImpl}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.{QuadBranch, QuadLeaf, QuadNode}

/**
  * Concrete QuadTree implementation of SearchTree
  * @param bb Initial bounding box describing the root bounds
  * @tparam EE Element type
  */
final class QuadTree[EE <: HasPosition] private (bb: AABB, val parameters: Parameters)
  extends SearchableContainer[EE] with RootedContainer[EE, QuadNode[EE]] with EuclideanSearches[EE] {
  override type NodeType = QuadNode[E]

  var root: QuadNode[E] = new QuadLeaf(bb, None, parameters)

  def addEnclose(e: E): Unit = {
    if(root.encloses(e)) {
      add(e)
    } else{
      val newAABB = QuadTreeUtils.expandedAABB(e.position, root.bounds)
      val newRoot = new QuadBranch[E](newAABB, None, parameters)
      newRoot.setChild(QuadTreeUtils.findQuadrant(root.bounds.center, newRoot.bounds), root)
      root = newRoot
      addEnclose(e)
    }
  }

  override def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: EE => Boolean): Seq[EE] = ???

  override def seqKnnSearch(queryPoints: IndexedSeq[Float2], k: Int): Seq[EE] = ???

  override def seqRangeSearch(queryPoints: IndexedSeq[Float2], r: Float): Seq[EE] = ???

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = ???

}

object QuadTree{

  def apply[E <: HasPosition](bb: AABB, parameters: Parameters): QuadTree[E] = {
    new QuadTree[E](AABB.enclosingSquare(bb), parameters)
  }

  def apply[E <: HasPosition](bb: AABB, points: Seq[E], parameters: Parameters): QuadTree[E] = {
    val tree = QuadTree[E](bb, parameters)
    points.foreach{ p =>
      require(bb.contains(p.position))
      tree.add(p)
    }
    tree
  }

  def apply[E <: HasPosition](bb: AABB, points: Seq[E]): QuadTree[E] = QuadTree[E](bb, points, Parameters())

  def apply[E <: HasPosition](bb: AABB = AABB.unit): QuadTree[E] = QuadTree(bb, Parameters(bb))

  def apply[E <: HasPosition](elems: Seq[E]): QuadTree[E] = QuadTree(elems, Parameters())

  def apply[E <: HasPosition](elems: Seq[E], parameters: Parameters): QuadTree[E] = {
    val square = AABB.enclosingSquare(elems.map{_.position})
    require(square.area > 0,
      "At least two spatially distinct elements required for creating the initial node.")

    // if default, use params depending on root node size
    val p = if(parameters == Parameters()) Parameters(square) else parameters
    val q = QuadTree[E](square, p)
    q.add(elems)
    q
  }

}
