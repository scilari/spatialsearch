package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.BoundedSearchTree
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeUtils._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait QuadTreeLike[E <: Float2] extends BoundedSearchTree[E]{
    type NodeType = BoundedNode
    type BranchType = QuadBranch
    type LeafType = QuadLeaf

    class QuadBranch(
      bb: AABB,
      val parent: Option[BranchType] = None,
      parameters: Parameters
    ) extends NodeType(bb) with Branch {

      val children: Array[NodeType] = {
        val parent = Some(this)
        Array[NodeType](
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
      val parent: Option[BranchType] = None,
      parameters: Parameters
    ) extends NodeType(bb) with Leaf{
      val elements: mutable.Buffer[E] = new ArrayBuffer[E]()

      def splitCondition: Boolean =
        elements.lengthCompare(parameters.nodeElementCapacity) > 0 && width > parameters.minNodeSize

      def toNode: BranchType = new BranchType(this, this.parent, parameters)

    }
}


