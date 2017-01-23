package com.scilari.geometry.spatialsearch
import com.scilari.geometry.models.{AABB, Float2}
import QuadTreeUtils._
import com.scilari.geometry.spatialsearch.Tree.{Leaf, Node}

import scala.collection.mutable.ListBuffer

/**
  * Created by iv on 1/17/2017.
  */
abstract class QuadTree[E <: Float2](
  bb: AABB,
  val parent: QuadNode[E] = null
) extends AABB(bb) with Tree[Float2]{
  def add(elem: E): QuadTree[E]
}

class QuadNode[E <: Float2](
  bb: AABB,
  parent: QuadNode[E] = null
) extends QuadTree[E](bb, parent) with Node[Float2, QuadTree[E]]{
  val children = Array[QuadTree[E]](
    new QuadLeaf(topLeftAABB(this), this),
    new QuadLeaf(topRightAABB(this), this),
    new QuadLeaf(bottomLeftAABB(this), this),
    new QuadLeaf(bottomRightAABB(this), this)
  )

  def add(elem: E) = {
    val q = findQuadrant(elem, this)
    val child = getChild(q).add(elem)
    setChild(q, child)
    this
  }

  def isRoot: Boolean = parent == null

  def setChild(ix: Int, node: QuadTree[E]): Unit = children(ix) = node
  def getChild(ix: Int): QuadTree[E] = children(ix)

}

class QuadLeaf[T <: Float2](
  bb: AABB,
  parent: QuadNode[T] = null
) extends QuadTree[T](bb, parent) with Leaf[Float2, T]{
  val children = ListBuffer[T]()


  import QuadTree._
  def add(elem: T) = {
    children += elem
    if(children.size > nodeElementCapacity && width >= minNodeSize ) split() else this
  }

  def split(): QuadNode[T] = {
    val newParent = new QuadNode[T](this)
    children.foreach(newParent.add)
    newParent
  }


  object QuadTree{
    var nodeElementCapacity = 16
    var minNodeSize = 0.0000001f
  }


}
