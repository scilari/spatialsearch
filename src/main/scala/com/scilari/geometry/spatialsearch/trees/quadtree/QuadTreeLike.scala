package com.scilari.geometry.spatialsearch.trees.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.core.Tree.{Branch, Leaf, Node}
import com.scilari.geometry.spatialsearch.searches.euclidean.Bounded
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeUtils._

import scala.collection.mutable.{ArrayBuffer}

object QuadTreeLike{

  trait QuadNode[E <: Float2] extends Node[E, QuadNode[E]] with Bounded {
    def bounds: AABB
    def encloses(e: E): Boolean = bounds.contains(e)
  }

  final class QuadBranch[E <: Float2](
    val bounds: AABB,
    val parent: Option[QuadNode[E]] = None,
    parameters: Parameters
  ) extends QuadNode[E] with Branch[E, QuadNode[E]] {
    type NodeType = QuadNode[E]

    private[this] val thisAsParent = Some[NodeType](this)
    private[this] def hhw = bounds.halfWidth/2
    private[this] var child0: NodeType = new QuadLeaf(topLeftAABB(bounds.center, hhw), thisAsParent, parameters)
    private[this] var child1: NodeType = new QuadLeaf(topRightAABB(bounds.center, hhw), thisAsParent, parameters)
    private[this] var child2: NodeType = new QuadLeaf(bottomLeftAABB(bounds.center, hhw), thisAsParent, parameters)
    private[this] var child3: NodeType = new QuadLeaf(bottomRightAABB(bounds.center, hhw), thisAsParent, parameters)

    def children: Array[NodeType] = Array(child0, child1, child2, child3)

    override def setChild(i: Int, c: NodeType): Unit = {
      i match {
        case 0 => child0 = c
        case 1 => child1 = c
        case 2 => child2 = c
        case 3 => child3 = c
      }
    }

    override def getChild(i: Int): NodeType = {
      i match {
        case 0 => child0
        case 1 => child1
        case 2 => child2
        case 3 => child3
      }
    }

    override def forEachChild(f: QuadNode[E] => Unit): Unit = {
      f(child0)
      f(child1)
      f(child2)
      f(child3)
    }

    def findChildIndex(elem: E): Int = findQuadrant(elem, bounds)

  }


  final class QuadLeaf[E <: Float2](
    val bounds: AABB,
    val parent: Option[QuadNode[E]] = None,
    parameters: Parameters
  ) extends QuadNode[E] with Leaf[E, QuadNode[E]]{
    type NodeType = QuadNode[E]
    val elements = new ArrayBuffer[E](parameters.nodeElementCapacity/4)

    def splitCondition: Boolean =
      elementCount > parameters.nodeElementCapacity && bounds.width > parameters.minNodeSize

    def toNode: NodeType = new QuadBranch(bounds, this.parent, parameters)

    @inline
    override def forEachElement(f: E => Unit): Unit = {
      var i = 0
      val n = elements.size
      while(i < n){
        f(elements(i))
        i += 1
      }
    }

  }
}


