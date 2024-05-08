package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.spatialsearch.searches.EuclideanSearches
import Tree.Node
import com.scilari.geometry.models.{AABB, Position}
import scala.collection.mutable.{ArrayBuffer, ArraySeq}

final case class UnionTree[E <: Position](initialNodes: List[Node[E]])
    extends EuclideanSearches[E] {}

object UnionTree {
  def fromTrees[E <: Position](trees: List[QuadTree[E]]): UnionTree[E] =
    UnionTree[E](trees.map(_.root))
}
