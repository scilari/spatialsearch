package com.scilari.geometry.spatialsearch.core

import scala.collection.mutable
import scala.collection.mutable.{ArraySeq, Buffer}


object Tree{

  trait Node[E, NodeType <: Node[E, NodeType]]{
    this: NodeType =>
    
    def foreach[U](f: E => U): Unit = elements.foreach(f)
    
    def size: Int = elementCount
    
    def isEmpty: Boolean = size == 0
    
    def nonEmpty: Boolean = !isEmpty

    def elements: Buffer[E]

    def children: ArraySeq[NodeType]

    def nonEmptyIfNotEmptied: Boolean

    def elementCount: Int

    def nodes: collection.Seq[NodeType]

    def leaves: Buffer[NodeType]

    def depth: Int

    def childCount: Int

    def add(e: E): NodeType

    def add(elems: Seq[E]): NodeType = {
      var node: NodeType = this
      elems.foreach{ e =>
        node = add(e)
      }
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
    
    def elements: Buffer[E] = children.flatMap(_.elements).toBuffer

    def nonEmptyIfNotEmptied: Boolean = true

    def elementCount: Int = children.map{_.elementCount}.sum

    def nodes: collection.Seq[NodeType] = collection.Seq(this) ++ children.flatMap{ _.nodes }

    def leaves: Buffer[NodeType] = children.flatMap {c => c.leaves }.toBuffer

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

    val elements: Buffer[E]

    override def children: ArraySeq[NodeType] = ???

    def nonEmptyIfNotEmptied: Boolean = elements.nonEmpty

    def elementCount: Int = elements.length

    def nodes: collection.Seq[NodeType] = collection.Seq(this)

    def leaves: Buffer[NodeType] = Buffer(this)

    def depth: Int = 1
    
    def childCount: Int = ???
    
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
