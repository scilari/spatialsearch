package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.spatialsearch.searches.euclidean.{EuclideanSearches, SeqSearches}
import QuadTree.Node
import com.scilari.geometry.spatialsearch.core.Tree
import com.scilari.geometry.models.{AABB, Position}
import scala.collection.mutable.ArraySeq
import scala.collection.mutable.ArrayBuffer

final case class MultiTree[E <: Position](roots: Iterable[Node[E]])
    extends EuclideanSearches[E]
    with SeqSearches[E] {
  var root: Node[E] = MultiTree.MultiBranch[E](ArrayBuffer.from(roots))
}

object MultiTree {
  def fromTrees[E <: Position](trees: Seq[QuadTree[E]]): MultiTree[E] =
    MultiTree[E](trees.map(_.root))
  class MultiBranch[E <: Position](var children: ArrayBuffer[Node[E]])
      extends Node[E]
      with Tree.Branch[E, Node[E]] {
    def findChildIndex(e: E): Int = ???
    def getChild(i: Int): Node[E] = ???
    def setChild(i: Int, c: Node[E]): Unit = ???
    var bounds: com.scilari.geometry.models.AABB = AABB.unit
    def compress(): Unit = ???
    def parent: Option[Node[E]] = None
  }
}
