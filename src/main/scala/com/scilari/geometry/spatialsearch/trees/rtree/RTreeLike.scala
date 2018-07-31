package com.scilari.geometry.spatialsearch.trees.rtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait RTreeLike[E <: Float2] extends BoundedSearchTree[E] {
    type BaseType = BoundedBase
    type NodeType = RTreeNode
    type LeafType = RTreeLeaf

    class RTreeNode(
      bb: AABB,
      elems: Seq[E],
      val parent: Option[RTreeNode],
      nodeElementCapacity: Int
    ) extends BaseType(bb) with Node{

      elems.foreach(enclose)

      val children: Array[BaseType] = {
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

      override def add(e: E): NodeType = {
        enclose(e)
        super.add(e)
      }

      def findChildIndex(e: E): Int =
        if(RTreeUtils.isEnclosingFirst(children(0), children(1), e)) 0 else 1

    }

    class RTreeLeaf(
      bb: AABB,
      val parent: Option[NodeType] = None,
      nodeElementCapacity: Int
    ) extends BaseType(bb) with Leaf {

      val elements: mutable.Buffer[E] = ArrayBuffer[E]()

      override def add(e: E): BaseType = {
        enclose(e)
        super.add(e)
      }

      def splitCondition: Boolean = elements.lengthCompare(nodeElementCapacity) > 0

      override def add(elems: Seq[E]): BaseType = {
        elems.foreach(enclose)
        elements ++= elems
        if(splitCondition) split() else this
      }

      def toNode: NodeType = new NodeType(this, elements, parent, nodeElementCapacity)

      override def split(): NodeType = {
        val newParent = toNode
        newParent
      }
    }

}
