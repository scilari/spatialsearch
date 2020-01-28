package com.scilari.geometry.spatialsearch.core

import scala.collection.mutable

object Tree{

  trait Node[E, NodeType <: Node[E, NodeType]]{
    this: NodeType =>

    def foreach[U](f: E => U): Unit = elements.foreach(f)

    def elements: mutable.Buffer[E]

    def children: Array[NodeType]

    def forEachChild(f: NodeType => Unit): Unit

    def forEachElement(f: E => Unit): Unit

    def nonEmptyIfNotEmptied: Boolean

    def elementCount: Int

    def nodes: Seq[NodeType]

    def leaves: Seq[NodeType]

    def depth: Int

    def childCount: Int

    def add(e: E): NodeType

    def add(elems: Seq[E]): NodeType = {
      var node: NodeType = this
      val n = elems.size
      var i = 0
      while(i < n){
        node = add(elems(i))
        i += 1
      }
      //elems.foreach(e => node = add(e)) // linter:ignore VariableAssignedUnusedValue
      node
    }

    def parent: Option[NodeType]

    def isRoot: Boolean = parent.isEmpty

    def isLeaf: Boolean

    def nonLeaf: Boolean = !isLeaf

    def encloses(e: E): Boolean

    def remove(e: E): Unit
  }


  trait Branch[E, NodeType <: Node[E, NodeType]] {
    this: NodeType =>

    def elements: mutable.Buffer[E] = children.flatMap(_.elements).toBuffer

    override def forEachElement(f: E => Unit): Unit = ()

    def nonEmptyIfNotEmptied: Boolean = true

    def elementCount: Int = children.map{_.elementCount}.sum

    def nodes: Seq[NodeType] = Seq(this) ++ children.flatMap {
      _.nodes
    }

    def leaves: Seq[NodeType] = children.flatMap {
      _.leaves
    }

    def depth: Int = children.map {
      _.depth
    }.max + 1

    def childCount: Int = children.length

    def isLeaf: Boolean = false

    def remove(e: E): Unit = children.filter{_.encloses(e)}.foreach(_.remove(e))

    def findChildIndex(e: E): Int

    def getChild(i: Int): NodeType

    def setChild(i: Int, c: NodeType): Unit

    def add(e: E): NodeType = {
      val ix = findChildIndex(e)
      val child = getChild(ix)
      val newChild = child.add(e)
      setChild(ix, newChild)
      this
    }

  }

  trait Leaf[E, NodeType <: Node[E, NodeType]] {
    this: NodeType =>

    val elements: mutable.Buffer[E]

    override def children: Array[NodeType] = ???

    override def forEachChild(f: NodeType => Unit): Unit = ()

    def nonEmptyIfNotEmptied: Boolean = elements.nonEmpty

    def elementCount: Int = elements.size

    def nodes: Seq[NodeType] = Seq(this)

    def leaves: Seq[NodeType] = List(this)

    def depth: Int = 1

    def childCount: Int = elements.size

    def isLeaf: Boolean = true

    def remove(e: E): Unit = elements -= e

    def add(e: E): NodeType = {
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
