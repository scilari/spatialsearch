package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.spatialsearch.searches.euclidean.{EuclideanSearches, SeqSearches}
import QuadTree.Node
import com.scilari.geometry.spatialsearch.core.Tree
import com.scilari.geometry.models.{AABB, Position}
import scala.collection.mutable.ArraySeq

final case class MultiQuadTree[E <: Position](roots: ArraySeq[Node[E]]) 
extends EuclideanSearches[E] with SeqSearches[E] {
    var root: Node[E] = MultiQuadTree.MultiBranch[E](roots)    
}

object MultiQuadTree {
    class MultiBranch[E <: Position](var children: ArraySeq[Node[E]]) extends Node[E]
    with Tree.Branch[E, Node[E]] {
        def findChildIndex(e: E): Int = ???
        def getChild(i: Int): Node[E] = ???
        def setChild(i: Int, c: Node[E]): Unit = ???
        var bounds: com.scilari.geometry.models.AABB = AABB.unit
        def compress(): Unit = ???
        def parent: Option[Node[E]] = None
    }
}