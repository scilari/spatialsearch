package com.scilari.geometry.spatialsearch.core

import com.scilari.geometry.models.Position
import com.scilari.geometry.spatialsearch.quadtree.QuadTree.Node

trait Rooted[E <: Position] {
  var root: Node[E]
}
