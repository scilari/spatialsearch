package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.{AABB, Position}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

trait Rooted[E <: Position] {
  // TODO: make this more general (or move to QuadTree)
  type RootType = QuadTree.Node[E]
  var root: RootType
}
