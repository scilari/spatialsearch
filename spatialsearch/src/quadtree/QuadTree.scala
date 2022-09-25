package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2, Position}
import com.scilari.geometry.spatialsearch.core.RootedContainer

import scala.collection.mutable.{ArrayBuffer, ArraySeq}
import com.scilari.geometry.spatialsearch.core.SearchConfig
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig
import com.scilari.geometry.spatialsearch.searches.EuclideanSearches
import com.scilari.geometry.spatialsearch.searches.ManhattanSearches
import com.scilari.geometry.spatialsearch.quadtree.Tree.{Node, Leaf}

/** Concrete QuadTree implementation
  * @param bb
  *   Initial bounding box describing the root bounds
  * @tparam E
  *   Element type
  */
final class QuadTree[E <: Position] private (bb: AABB, val parameters: Parameters)
    extends EuclideanSearches[E]
    with RootedContainer[E] {

  var root: Node[E] = Leaf(bb, None, parameters)
  def initialNodes = List(root)

  object Manhattan extends ManhattanSearches[E] {
    def initialNodes = List(root)
  }

}

object QuadTree {

  def apply[E <: Position](bb: AABB, parameters: Parameters): QuadTree[E] = {
    new QuadTree[E](if (bb.isSquare) bb else AABB.enclosingSquare(bb), parameters)
  }

  def apply[E <: Position](
      bb: AABB,
      points: Iterable[E],
      parameters: Parameters
  ): QuadTree[E] = {
    val tree = QuadTree[E](bb, parameters)
    points.foreach { p =>
      require(
        bb.contains(p.position)
      ) // TODO: will the insert be even faster if this is not checked?
      tree.add(p)
    }
    tree
  }

  def apply[E <: Position](bb: AABB): QuadTree[E] = apply(bb, Parameters(bb))

  def apply[E <: Position](bb: AABB, points: Iterable[E]): QuadTree[E] =
    QuadTree[E](bb, points, Parameters(bb))

  def apply[E <: Position](elems: Iterable[E]): QuadTree[E] =
    QuadTree(elems, Parameters.default)

  def apply[E <: Position](elems: Iterable[E], parameters: Parameters): QuadTree[E] = {
    val square = AABB.enclosingSquare(elems.map { _.position })
    require(
      square.area > 0,
      "At least two spatially distinct elements required for creating the initial node."
    )

    // if default, use params depending on root node size
    val p = if (parameters == Parameters.default) Parameters(square) else parameters
    val q = QuadTree[E](square, p)
    q.add(elems)
    q
  }

}
