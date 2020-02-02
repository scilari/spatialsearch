package com.scilari.geometry.spatialsearch.searches.euclidean

import com.scilari.geometry.models.HasPosition
import com.scilari.geometry.spatialsearch.searches.base.Knn
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.QuadNode

final class KnnImpl[E <: HasPosition](var root: QuadNode[E], val k: Int) extends Knn with EuclideanTypes[E]
