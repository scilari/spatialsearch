package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.AABB
import com.scilari.geometry.spatialsearch.trees.quadtree.Parameters

trait SearchTree[E] extends Searchable[E] with SpatialContainer[E]

object SearchTree{
  import trees.quadtree.{QuadTree => QT}
  def QuadTree[E](bb: AABB, p: Parameters): SearchTree[E] = QT[E](bb, p)
  def QuadTree[E](bb: AABB): SearchTree[E] = QT[E](bb)
  def QuadTree[E](elements: Seq[E], p: Parameters): SearchTree[E] = QT[E](elements, p)
  def QuadTree[E](elements: Seq[E]): SearchTree[E] = QT[E](elements)

  import trees.multitree.{MultiTree => MT}
  def MultiTree[E](trees: Seq[QT[E]]): Searchable[E] = MT[E](trees)
}
