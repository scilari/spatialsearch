package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2, HasPosition}
import com.scilari.geometry.spatialsearch.core.Tree.{Branch, Leaf, Node}
import com.scilari.geometry.spatialsearch.searches.euclidean.Bounded
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeUtils._

import scala.collection.mutable.ArrayBuffer

object QuadTreeLike{

  trait QuadNode[E <: HasPosition] extends Node[E, QuadNode[E]] with Bounded {
    def bounds: AABB
    def encloses(e: E): Boolean = bounds.contains(e.position)
  }

  final class QuadBranch[E <: HasPosition](
    val bounds: AABB,
    val parent: Option[QuadNode[E]] = None,
    parameters: Parameters
  ) extends QuadNode[E] with Branch[E, QuadNode[E]] {
    type NodeType = QuadNode[E]

    val children: Array[NodeType] = {
      val thisAsParent = Some[NodeType](this)
      def hhw = bounds.halfWidth/2
      Array(
        new QuadLeaf(topLeftAABB(bounds.center, hhw), thisAsParent, parameters),
        new QuadLeaf(topRightAABB(bounds.center, hhw), thisAsParent, parameters),
        new QuadLeaf(bottomLeftAABB(bounds.center, hhw), thisAsParent, parameters),
        new QuadLeaf(bottomRightAABB(bounds.center, hhw), thisAsParent, parameters)
      )
    }

    override def setChild(i: Int, c: NodeType): Unit = children(i) = c
    override def getChild(i: Int): NodeType = children(i)

    def findChildIndex(elem: E): Int = findQuadrant(elem.position, bounds)
  }


  final class QuadLeaf[E <: HasPosition](
    val bounds: AABB,
    val parent: Option[QuadNode[E]] = None,
    parameters: Parameters
  ) extends QuadNode[E] with Leaf[E, QuadNode[E]]{
    type NodeType = QuadNode[E]
    val elements = new ArrayBuffer[E](parameters.nodeElementCapacity/4)

    def splitCondition: Boolean =
      elementCount > parameters.nodeElementCapacity && bounds.width > parameters.minNodeSize

    def toNode: NodeType = new QuadBranch(bounds, this.parent, parameters)
  }
}


