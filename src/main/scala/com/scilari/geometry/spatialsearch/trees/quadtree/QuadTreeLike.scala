package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeUtils._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait QuadTreeLike[E <: Float2] extends BoundedSearchTree[E]{
    type BaseType = BoundedBase
    type NodeType = QuadNode
    type LeafType = QuadLeaf

    class QuadNode(
      bb: AABB,
      val parent: Option[NodeType] = None,
      parameters: Parameters
    ) extends BaseType(bb) with Node {

      val children: Array[BaseType] = {
        val parent = Some(this)
        Array[BaseType](
          new LeafType(topLeftAABB(this), parent, parameters),
          new LeafType(topRightAABB(this), parent, parameters),
          new LeafType(bottomLeftAABB(this), parent, parameters),
          new LeafType(bottomRightAABB(this), parent, parameters)
        )
      }

      def findChildIndex(elem: E): Int = findQuadrant(elem, this)
    }

    class QuadLeaf(
      bb: AABB,
      val parent: Option[NodeType] = None,
      parameters: Parameters
    ) extends BaseType(bb) with Leaf{
      val elements: mutable.Buffer[E] = new ArrayBuffer[E]()

      def splitCondition: Boolean =
        elements.lengthCompare(parameters.nodeElementCapacity) > 0 && width >= parameters.minNodeSize

      def toNode: NodeType = new NodeType(this, this.parent, parameters)

    }
}


