package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.searches.base.Radius
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.QuadNode

// TODO: refactor to use bounded
class RadiusImpl[E <: Float2](var root: QuadNode[E], r: Float) extends Radius with EuclideanTypes[E]{
  val rSq: Float = r * r
}
