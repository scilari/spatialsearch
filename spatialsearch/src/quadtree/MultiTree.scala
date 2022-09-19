package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.spatialsearch.searches.EuclideanSearches
import QuadTreeStructure.Node
import com.scilari.geometry.models.{AABB, Position}
import scala.collection.mutable.{ArrayBuffer, ArraySeq}

final case class MultiTree[E <: Position](initialNodes: List[Node[E]])
    extends EuclideanSearches[E] {}

object MultiTree {
  def fromTrees[E <: Position](trees: List[QuadTree[E]]): MultiTree[E] =
    MultiTree[E](trees.map(_.root))
}
