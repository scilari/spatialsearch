package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.core.Tree.{Branch, Leaf, Node}
import com.scilari.geometry.spatialsearch.searches.euclidean.Bounded
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeUtils._

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.reflect.ClassTag

object QuadTreeLike{

  trait QuadNode[E <: Float2] extends Node[E, QuadNode[E]] with Bounded {
    def bounds: AABB
    def encloses(e: E): Boolean = bounds.contains(e)
  }

  class QuadBranch[E <: Float2](
    val bounds: AABB,
    val parent: Option[QuadNode[E]] = None,
    parameters: Parameters
  ) extends QuadNode[E] with Branch[E, QuadNode[E]] {
    type NodeType = QuadNode[E]

    val children: Array[NodeType] = {
      val parent = Some[NodeType](this)
      val hhw = bounds.halfWidth/2
      Array[QuadNode[E]](
        new QuadLeaf(topLeftAABB(bounds.center, hhw), parent, parameters),
        new QuadLeaf(topRightAABB(bounds.center, hhw), parent, parameters),
        new QuadLeaf(bottomLeftAABB(bounds.center, hhw), parent, parameters),
        new QuadLeaf(bottomRightAABB(bounds.center, hhw), parent, parameters)
      )
    }

    def findChildIndex(elem: E): Int = findQuadrant(elem, bounds)

  }


  class QuadLeaf[E <: Float2](
    val bounds: AABB,
    val parent: Option[QuadNode[E]] = None,
    parameters: Parameters
  ) extends QuadNode[E] with Leaf[E, QuadNode[E]]{
    type NodeType = QuadNode[E]
    val elements = new ArrayBuffer[E](12)

    def splitCondition: Boolean =
      elementCount > parameters.nodeElementCapacity && bounds.width > parameters.minNodeSize

    def toNode: NodeType = new QuadBranch(bounds, this.parent, parameters)

  }
}


