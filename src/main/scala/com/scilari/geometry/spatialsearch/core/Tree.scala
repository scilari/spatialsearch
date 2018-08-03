package com.scilari.geometry.spatialsearch.core

import scala.collection.mutable

trait Tree[E] {
  type NodeType <: Node
  type BranchType <: NodeType with Branch
  type LeafType <: NodeType with Leaf

  trait Node {
    this: NodeType =>

    def elements: Seq[E]

    def nodes: Seq[NodeType]

    def leaves: Seq[LeafType]

    def depth: Int

    def childCount: Int

    def add(e: E): NodeType

    def add(elems: Seq[E]): NodeType = {
      var n: NodeType = this
      elems.foreach(e => n = add(e)) // linter:ignore VariableAssignedUnusedValue
      n
    }

    def parent: Option[NodeType]

    def isRoot: Boolean = parent.isEmpty

    def isLeaf: Boolean

    def nonLeaf: Boolean = !isLeaf

    def isEmpty: Boolean = !nonEmpty

    def nonEmpty: Boolean

    def encloses(e: E): Boolean

    def remove(e: E): Unit
  }


  trait Branch extends Node{
    this: BranchType =>

    val children: Array[NodeType]

    def elements: Seq[E] = children.flatMap(_.elements)

    def nodes: Seq[NodeType] = Seq(this) ++ children.flatMap {
      _.nodes
    }

    def leaves: Seq[LeafType] = children.flatMap {
      _.leaves
    }

    def depth: Int = children.map {
      _.depth
    }.max + 1

    def childCount: Int = children.length

    def isLeaf: Boolean = false

    def nonEmpty: Boolean = leaves.exists(_.nonEmpty)

    def remove(e: E): Unit = children.filter{_.encloses(e)}.foreach(_.remove(e))

    def findChildIndex(e: E): Int

    def getChild(i: Int): NodeType = children(i)

    def setChild(i: Int, c: NodeType): Unit = children(i) = c

    def add(e: E): BranchType = {
      val ix = findChildIndex(e)
      val child = getChild(ix)
      val newChild = child.add(e)
      setChild(ix, newChild)
      this
    }
  }

  trait Leaf extends Node {
    this: LeafType =>

    val elements: mutable.Buffer[E]

    def nodes: Seq[NodeType] = Seq(this)

    def leaves: Seq[LeafType] = Seq(this)

    val depth: Int = 1

    def childCount: Int = elements.size

    def isLeaf: Boolean = true

    def nonEmpty: Boolean = elements.nonEmpty

    def remove(e: E): Unit = elements -= e

    def add(e: E): NodeType = {
      elements += e
      if (splitCondition) split() else this
    }

    def splitCondition: Boolean

    def split(): BranchType = {
      val newNode = toNode
      elements.foreach(newNode.add)
      newNode
    }

    def toNode: BranchType
  }

}





