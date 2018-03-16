package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.MetricObject

import scala.collection.mutable


trait Tree[P, E]{
  type BaseType <: Base
  type NodeType <: BaseType with Node
  type LeafType <: BaseType with Leaf

  trait Base extends MetricObject[P] with Traversable[E] {
    def elements: Seq[E]

    def nodes: Seq[BaseType]

    def leaves: Seq[LeafType]

    override def foreach[U](f: E => U): Unit = elements.foreach(f)

    def depth: Int

    def childCount: Int

    def add(e: E): BaseType

    def add(elems: Seq[E]): BaseType = {
      var n: BaseType = this.asInstanceOf[BaseType]
      elems.foreach(e => n = add(e)) // linter:ignore VariableAssignedUnusedValue
      n
    }

    def parent: Option[BaseType]

    def isRoot: Boolean = parent.isEmpty

    def isLeaf: Boolean

    def nonLeaf: Boolean = !isLeaf

    def contains(e: P): Boolean = zeroDistance(e)

  }


  trait Node extends Base {
    this: NodeType =>
    val children: Array[BaseType]

    def elements: Seq[E] = children.flatMap(_.elements)

    def nodes: Seq[BaseType] = Seq(this) ++ children.flatMap{_.nodes}

    def leaves: Seq[LeafType] = children.flatMap{_.leaves}

    def depth: Int = children.map {
      _.depth
    }.max + 1

    def childCount: Int = children.length

    def isLeaf: Boolean = false

    def findChildIndex(e: E): Int

    def getChild(i: Int): BaseType = children(i)

    def setChild(i: Int, c: BaseType): Unit = children(i) = c

    def add(e: E): NodeType = {
      val ix = findChildIndex(e)
      val child = getChild(ix)
      val newChild = child.add(e)
      setChild(ix, newChild)
      this
    }
  }

  trait Leaf extends Base {
    this: LeafType =>
    val elements: mutable.Buffer[E]

    def nodes: Seq[BaseType] = Seq(this)

    def leaves: Seq[LeafType] = Seq(this)

    def depth: Int = 1

    def childCount: Int = elements.size

    def isLeaf: Boolean = true

    def add(e: E): BaseType = {
      elements += e
      if (splitCondition) split() else this
    }

    def splitCondition: Boolean

    def split(): NodeType = {
      val newNode = toNode
      elements.foreach(newNode.add)
      newNode
    }

    def toNode: NodeType
  }

}





