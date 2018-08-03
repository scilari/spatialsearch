package com.scilari.geometry.spatialsearch.trees.rtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait RTreeLike[E <: Float2] extends BoundedSearchTree[E] {
    type NodeType = BoundedNode
    type BranchType = RTreeBranch
    type LeafType = RTreeLeaf

    class RTreeBranch(
      bb: AABB,
      elems: Seq[E],
      val parent: Option[RTreeBranch],
      nodeElementCapacity: Int
    ) extends NodeType(bb) with Branch{

      elems.foreach(enclose)

      val children: Array[NodeType] = {
        val (boxA, boxB) = RTreeUtils.angLinearSplit(this, elems)
        Array(
          new LeafType(boxA, Some(this), nodeElementCapacity),
          new LeafType(boxB, Some(this), nodeElementCapacity)
        )
      }

      {
        val (for0, for1) = elems.partition(e => children(0).encloses(e))
        setChild(0, getChild(0).add(for0))
        setChild(1, getChild(1).add(for1))
      }

      override def add(e: E): BranchType = {
        enclose(e)
        super.add(e)
      }

      def findChildIndex(e: E): Int =
        if(RTreeUtils.isEnclosingFirst(children(0), children(1), e)) 0 else 1

    }

    class RTreeLeaf(
      bb: AABB,
      val parent: Option[BranchType] = None,
      nodeElementCapacity: Int
    ) extends NodeType(bb) with Leaf {

      val elements: mutable.Buffer[E] = ArrayBuffer[E]()

      override def add(e: E): NodeType = {
        enclose(e)
        super.add(e)
      }

      def splitCondition: Boolean = elements.lengthCompare(nodeElementCapacity) > 0

      override def add(elems: Seq[E]): NodeType = {
        elems.foreach(enclose)
        elements ++= elems
        if(splitCondition) split() else this
      }

      def toNode: BranchType = new BranchType(this, elements, parent, nodeElementCapacity)

      override def split(): BranchType = {
        val newParent = toNode
        newParent
      }
    }

}
