package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.trees.quadtree.Parameters

trait SearchTree[E] extends Searchable[E] with SpatialContainer[E]

object SearchTree{
  import trees.quadtree.{QuadTree => QT}
  import trees.multitree.{MultiTree => MT}

  object QuadTree{
    def apply[E <: Float2](bb: AABB, p: Parameters): SearchTree[E] = QT[E](bb, p)
    def apply[E <: Float2](bb: AABB): SearchTree[E] = QT[E](bb)
    def apply[E <: Float2](elements: Seq[E], p: Parameters): SearchTree[E] = QT[E](elements, p)
    def apply[E <: Float2](elements: Seq[E]): SearchTree[E] = QT[E](elements)
  }

  object MultiTree{
    def apply[E <: Float2](trees: Seq[QT[E]]): Searchable[E] = MT[E](trees)
  }

}
