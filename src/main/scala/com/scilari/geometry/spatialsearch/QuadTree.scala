package com.scilari.geometry.spatialsearch
import com.scilari.geometry.models.{AABB, Float2}
import QuadTreeUtils._
import com.scilari.geometry.spatialsearch.Tree.{Leaf, Node}

import scala.collection.mutable
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
  val children: Array[QuadTree[E]] = Array[QuadTree[E]](
    new QuadLeaf(topLeftAABB(this), this),
    new QuadLeaf(topRightAABB(this), this),
    new QuadLeaf(bottomLeftAABB(this), this),
    new QuadLeaf(bottomRightAABB(this), this)
  )

  def add(elem: E): QuadNode[E] = {
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
  val children: mutable.Buffer[T] = ListBuffer[T]()


  import Parameters._
  def add(elem: T): QuadTree[T] = {
    children += elem
    if(children.size > nodeElementCapacity && width >= minNodeSize ) split() else this
  }

  def split(): QuadNode[T] = {
    val newParent = new QuadNode[T](this)
    children.foreach(newParent.add)
    newParent
  }


  object Parameters{
    var nodeElementCapacity = 16
    var minNodeSize = 0.0000001f
  }


}

object QuadTreeUtils {
  val topLeftIndex = 0
  val topRightIndex = 1
  val bottomLeftIndex = 2
  val bottomRightIndex = 3


  def topLeftAABB(b: AABB) = AABB(b.minPoint.x, b.centerY, b.centerX, b.maxPoint.y)
  def topRightAABB(b: AABB) = AABB(b.center, b.maxPoint)
  def bottomLeftAABB(b: AABB) = AABB(b.minPoint, b.center)
  def bottomRightAABB(b: AABB) = AABB(b.centerX, b.minPoint.y, b.maxPoint.x, b.centerY)

  def topLeftAABB(b: AABB, centerPoint: Float2) = AABB(b.minPoint.x, centerPoint.y, centerPoint.x, b.maxPoint.y)
  def topRightAABB(b: AABB, centerPoint: Float2) = AABB(centerPoint, b.maxPoint)
  def bottomLeftAABB(b: AABB, centerPoint: Float2) = AABB(b.minPoint, centerPoint)
  def bottomRightAABB(b: AABB, centerPoint: Float2) = AABB(centerPoint.x, b.minPoint.y, b.maxPoint.x, centerPoint.y)

  def quadrantByIndex(b: AABB, index: Int): AABB = {
    index match{
      case `topLeftIndex` => topLeftAABB(b)
      case `topRightIndex` => topRightAABB(b)
      case `bottomLeftIndex` => bottomLeftAABB(b)
      case `bottomRightIndex` => bottomRightAABB(b)
    }
  }

  // Computes the AABB that has b as its children and that is most towards given point
  def enclosingAABB(point: Float2, b: AABB): AABB ={
    val corner = b.closestCorner(point)
    new AABB(center = corner, halfWidth = b.width)
  }

  def findQuadrant(point: Float2, b: AABB): Int = {
    // This branch prediction friendly implementation seems to be over three times faster than simple if-based one
    val rowIncrement = if(point.x <= b.centerX) 0 else 1
    val columnIncrement = if(point.y <= b.centerY) 2 else 0
    rowIncrement + columnIncrement
  }


  def findQuadrant(point: Float2, centerPoint: Float2): Int = {
    val rowIncrement = if(point.x <= centerPoint.x) 0 else 1
    val columnIncrement = if(point.y <= centerPoint.y) 2 else 0
    rowIncrement + columnIncrement
  }

}

