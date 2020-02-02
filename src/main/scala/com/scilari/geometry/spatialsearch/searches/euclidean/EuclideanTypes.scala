package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.{Float2, HasPosition}
import com.scilari.geometry.spatialsearch.core.Types
import com.scilari.geometry.spatialsearch.searches.euclidean.Bounded.EuclideanBounded
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.QuadNode

trait EuclideanTypes[EE <: HasPosition] extends Types with EuclideanBounded {
  override type Q = Float2
  override type E = EE
  override type NodeType = QuadNode[E]

  var root: NodeType
}
