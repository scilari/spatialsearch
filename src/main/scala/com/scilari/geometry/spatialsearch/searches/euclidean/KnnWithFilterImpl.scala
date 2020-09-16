package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.HasPosition
import com.scilari.geometry.spatialsearch.searches.base.Knn
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.QuadNode

class KnnWithFilterImpl[EE <: HasPosition](var root: QuadNode[EE], val k: Int, condition: EE => Boolean)
  extends Knn with EuclideanTypes[EE] {
  override def filterElements(e: EE, s: State): Boolean = condition(e)
}
