package com.scilari.geometry.spatialsearch.quadtree
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils.Quadrants.*
import com.scilari.geometry.models.Position
import com.scilari.geometry.models.AABB
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArraySeq
import com.scilari.geometry.spatialsearch.quadtree.Parameters

// TODO: optimize elements and leaves etc.

object Tree {

  trait Node[E <: Position] {
    type NodeType = Node[E]

    var bounds: AABB
    def encloses(e: E): Boolean = bounds.contains(e.position)

    def compress(): Unit

    def foreach[U](f: E => U): Unit = elements.foreach(f)

    def size: Int = elementCount

    def isEmpty: Boolean = size == 0

    def nonEmpty: Boolean = !isEmpty

    def elements: ArrayBuffer[E]

    def children: ArraySeq[NodeType]

    def nonEmptyIfNotEmptied: Boolean

    def elementCount: Int

    def nodes: ArrayBuffer[NodeType]

    def leaves: ArrayBuffer[NodeType]

    def depth: Int

    def childCount: Int

    def add(e: E): NodeType

    def add(elems: Iterable[E]): NodeType = {
      var node: NodeType = this
      elems.foreach { e =>
        node = add(e)
      }
      node
    }

    def parent: Option[NodeType]

    def isRoot: Boolean = parent.isEmpty

    def isLeaf: Boolean

    def nonLeaf: Boolean = !isLeaf

    def remove(e: E): Unit
  }

  final class Branch[E <: Position](
      var bounds: AABB,
      val parent: Option[Node[E]] = None,
      parameters: Parameters
  ) extends Node[E] {

    var children: ArraySeq[Node[E]] = {
      val thisAsParent = Some[Node[E]](this)
      def hhw = bounds.halfWidth / 2
      ArraySeq(
        Leaf(topLeftAABB(bounds.center, hhw), thisAsParent, parameters),
        Leaf(topRightAABB(bounds.center, hhw), thisAsParent, parameters),
        Leaf(bottomLeftAABB(bounds.center, hhw), thisAsParent, parameters),
        Leaf(bottomRightAABB(bounds.center, hhw), thisAsParent, parameters)
      )
    }

    def setChild(i: Int, c: Node[E]): Unit = children(i) = c
    def getChild(i: Int): Node[E] = children(i)

    def findChildIndex(elem: E): Int = findQuadrant(elem.position, bounds)

    // Note: this breaks the tree structure for future additions
    def compress(): Unit = {
      children.foreach(_.compress())
      val nonEmptyChildren = children.filter(_.nonEmpty)
      children =
        if (nonEmptyChildren.size == 1 && !nonEmptyChildren.head.isLeaf)
          nonEmptyChildren.head.children
        else nonEmptyChildren
      bounds = AABB.fromPoints(children.map { _.bounds }.map { _.corners }.flatten)
    }

    def elements: ArrayBuffer[E] = ArrayBuffer.from(children).flatMap(_.elements)

    def nonEmptyIfNotEmptied: Boolean = true

    def elementCount: Int = children.map { _.elementCount }.sum

    def nodes: ArrayBuffer[NodeType] = ArrayBuffer.from(children).flatMap { _.nodes } += this

    def leaves: ArrayBuffer[NodeType] = ArrayBuffer.from(children).flatMap { c => c.leaves }

    def depth: Int = children.map {
      _.depth
    }.max + 1

    def childCount: Int = children.length

    def isLeaf: Boolean = false

    def remove(e: E): Unit = children.filter { _.encloses(e) }.foreach(_.remove(e))

    def add(e: E): NodeType = {
      val ix = findChildIndex(e)
      val child = getChild(ix)
      val newChild = child.add(e)
      setChild(ix, newChild)
      this
    }

  }

  final class Leaf[E <: Position](
      var bounds: AABB,
      val parent: Option[Node[E]] = None,
      parameters: Parameters
  ) extends Node[E] {
    val elements = new ArrayBuffer[E](parameters.nodeElementCapacity / 4)

    def children = ??? // TODO: clean up at some point

    def splitCondition: Boolean =
      elementCount >= parameters.nodeElementCapacity && bounds.width > parameters.minNodeSize

    def compress(): Unit = {
      bounds = AABB.fromPoints(elements.map { _.position })
    }

    def nonEmptyIfNotEmptied: Boolean = elements.nonEmpty

    def elementCount: Int = elements.length

    def nodes: ArrayBuffer[NodeType] = ArrayBuffer(this)

    def leaves: ArrayBuffer[NodeType] = ArrayBuffer(this)

    def depth: Int = 1

    def childCount: Int = ???

    def isLeaf: Boolean = true

    def remove(e: E): Unit = elements -= e

    def add(e: E): NodeType = {
      if (splitCondition) split(e)
      else {
        elements += e
        this
      }
    }

    def split(e: E): NodeType = {
      val newNode = new Branch(bounds, this.parent, parameters)
      elements.foreach(newNode.add)
      newNode.add(e)
      newNode
    }
  }

}
