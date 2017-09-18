package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.Tree
import com.scilari.geometry.spatialsearch.Tree.{Leaf, Node}
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by iv on 1/17/2017.
  */

/**
  * Base class for the Node and Leaf subclasses
  * @param bb Bounding box representing the tree boundaries
  * @tparam E Element type
  */
abstract class QuadTreeBase[E <: Float2](bb: AABB)
  extends AABB(bb) with Tree[Float2, E] {
  val parent: QuadNode[E]
  def add(elem: E): QuadTreeBase[E]
}

/**
  * Non-leaf Node
  * @param bb Bounding box representing the node boundaries
  * @param parent Reference to parent node
  * @tparam E Element type
  */
class QuadNode[E <: Float2](
  bb: AABB,
  val parent: QuadNode[E] = null
) extends QuadTreeBase[E](bb) with Node[Float2, E, QuadTreeBase[E]]{

  val children: mutable.Seq[QuadTreeBase[E]] = mutable.ArraySeq[QuadTreeBase[E]](
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

  def setChild(ix: Int, node: QuadTreeBase[E]): Unit = children(ix) = node
  def getChild(ix: Int): QuadTreeBase[E] = children(ix)

}

/**
  * Leaf
  * @param bb Bounding box representing the Leaf boundaries
  * @param parent Reference to parent node
  * @tparam E Element type
  */
class QuadLeaf[E <: Float2](
  bb: AABB,
  val parent: QuadNode[E] = null
) extends QuadTreeBase[E](bb) with Leaf[Float2, E]{
  val children: mutable.Buffer[E] = ListBuffer[E]()


  import Parameters._
  def add(elem: E): QuadTreeBase[E] = {
    children += elem
    if(children.size > nodeElementCapacity && width >= minNodeSize ) split() else this
  }


  def split(): QuadNode[E] = {
    val newParent = new QuadNode[E](this)
    children.foreach(newParent.add)
    newParent
  }

  object Parameters{
    var nodeElementCapacity = 16
    var minNodeSize = 0.01f
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
    // This branch prediction friendly implementation seems to be over three times faster than simple if-based one
    val rowIncrement = if(point.x <= centerPoint.x) 0 else 1
    val columnIncrement = if(point.y <= centerPoint.y) 2 else 0
    rowIncrement + columnIncrement
  }

}

