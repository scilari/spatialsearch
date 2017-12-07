package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.BoundedPlanarTree
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait QuadTreeLike[E <: Float2]{
  object Tree extends BoundedPlanarTree[E]{
    type BaseType = BoundedBase
    type NodeType = QuadNode
    type LeafType = QuadLeaf

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

      def toNode: NodeType = new NodeType(this, this.parent, parameters)

    }
  }
}


