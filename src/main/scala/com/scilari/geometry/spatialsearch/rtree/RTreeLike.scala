package com.scilari.geometry.spatialsearch.rtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.Tree

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RTreeLike[E <: Float2] {
  object Tree extends Tree[Float2, E]{
    type BaseType = RTreeBase
    type NodeType = RTreeNode
    type LeafType = RTreeLeaf

    abstract class RTreeBase(bb: AABB) extends AABB(bb) with Base{
      override def contains(p: Float2): Boolean = super[AABB].contains(p)
      override def toString(): String = "RTree: " + super[AABB].toString()
    }


    class RTreeNode(
      bb: AABB,
      elems: Seq[E],
      val parent: RTreeNode,
      nodeElementCapacity: Int
    ) extends BaseType(bb) with Node{

      elems.foreach(enclose)

      val children: Array[BaseType] = {
        val (boxA, boxB) = RTreeUtils.angLinearSplit(this, elems)
        Array(
          new LeafType(boxA, this, nodeElementCapacity),
          new LeafType(boxB, this, nodeElementCapacity)
        )
      }

      {
        val (for0, for1) = elems.partition(e => children(0).contains(e))
        setChild(0, getChild(0).add(for0))
        setChild(1, getChild(1).add(for1))
      }

      override def add(e: E): NodeType = {
        enclose(e)
        super.add(e)
      }

      def findChildIndex(e: E): Int =
        if(RTreeUtils.isEnclosingFirst(children(0).asInstanceOf[AABB], children(1).asInstanceOf[AABB], e)) 0 else 1

    }

    class RTreeLeaf(
      bb: AABB,
      val parent: NodeType = null,
      nodeElementCapacity: Int
    ) extends BaseType(bb) with Leaf {

      val elements: mutable.Buffer[E] = ListBuffer[E]()

      override def add(e: E): BaseType = {
        enclose(e)
        super.add(e)
      }

      def splitCondition: Boolean = elements.size > nodeElementCapacity

      override def add(elems: Seq[E]): BaseType = {
        //println("elem size: " + elems.size)
        elems.foreach(enclose)
        elements ++= elems
        if(splitCondition) split() else this
      }

      def toNode = new NodeType(this, elements, parent, nodeElementCapacity)

      override def split(): NodeType = {
        val newParent = toNode
        newParent
      }
    }

  }

}
