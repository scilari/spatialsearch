package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.Tree
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait QuadTreeLike[E <: Float2]{
  object Tree extends Tree[Float2, E]{
    type BaseType = QuadBase
    type NodeType = QuadNode
    type LeafType = QuadLeaf

    abstract class QuadBase(bb: AABB) extends AABB(bb) with Base{
      override def contains(p: Float2): Boolean = super[AABB].contains(p)
      override def toString(): String = "QuadTree: " + super[AABB].toString()
    }

    class QuadNode(
      bb: AABB,
      val parent: NodeType = null,
      parameters: Parameters
    ) extends BaseType(bb) with Node {

      val children: Array[BaseType] = Array[BaseType](
        new LeafType(topLeftAABB(this), this, parameters),
        new LeafType(topRightAABB(this), this, parameters),
        new LeafType(bottomLeftAABB(this), this, parameters),
        new LeafType(bottomRightAABB(this), this, parameters)
      )

      def findChildIndex(elem: E): Int = findQuadrant(elem, this)
    }

    class QuadLeaf(
      bb: AABB,
      val parent: NodeType = null,
      parameters: Parameters
    ) extends BaseType(bb) with Leaf{
      val elements: mutable.Buffer[E] = new ArrayBuffer[E]()

      def splitCondition: Boolean =
        elements.size > parameters.nodeElementCapacity && width >= parameters.minNodeSize

      def toNode = new NodeType(this, this.parent, parameters)

    }
  }
}


