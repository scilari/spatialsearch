package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.quadtree.{Parameters, QuadTree}


trait SearchTree[E <: Float2] extends Searchable[E] with SpatialContainer[E] {
  def toQuadTree: QuadTree[E] = this match {
    case t: QuadTree[E] => t
    case t: SearchTree[E] => QuadTree[E](t.elements)
  }

}

object SearchTree{
    def apply[E <: Float2](bb: AABB, p: Parameters): SearchTree[E] = QuadTree[E](bb, p)
    def apply[E <: Float2](bb: AABB): SearchTree[E] = QuadTree[E](bb)
    def apply[E <: Float2](elements: Seq[E], p: Parameters): SearchTree[E] = QuadTree[E](elements, p)
    def apply[E <: Float2](elements: Seq[E]): SearchTree[E] = QuadTree[E](elements)
}
