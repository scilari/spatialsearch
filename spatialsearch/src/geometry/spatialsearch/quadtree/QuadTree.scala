package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2, Position}
import com.scilari.geometry.spatialsearch.core.RootedContainer
import com.scilari.geometry.spatialsearch.searches.euclidean.{EuclideanSearches, SeqSearches}

import scala.collection.mutable.{ArrayBuffer, ArraySeq}
import com.scilari.geometry.spatialsearch.core.Tree

/** Concrete QuadTree implementation
  * @param bb
  *   Initial bounding box describing the root bounds
  * @tparam E
  *   Element type
  */
final class QuadTree[E <: Position] private (bb: AABB, val parameters: Parameters)
    extends RootedContainer[E]
    with EuclideanSearches[E]
    with SeqSearches[E] {

  type RootType = QuadTree.Node[E]

  var root: QuadTree.Node[E] = QuadTree.Leaf(bb, None, parameters)
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

  import com.scilari.geometry.spatialsearch.core.Tree
  import com.scilari.geometry.spatialsearch.quadtree.QuadTree.{Branch, Leaf, Node}
  import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils.{
    bottomLeftAABB,
    bottomRightAABB,
    findQuadrant,
    topLeftAABB,
    topRightAABB
  }

  trait Node[E <: Position] extends Tree.Node[E, Node[E]] {
    var bounds: AABB
    def encloses(e: E): Boolean = bounds.contains(e.position)

    def compress(): Unit
  }

  final class Branch[E <: Position](
      var bounds: AABB,
      val parent: Option[Node[E]] = None,
      parameters: Parameters
  ) extends Node[E]
      with Tree.Branch[E, Node[E]] {

    var children: ArraySeq[Node[E]] = {
      val thisAsParent = Some[Node[E]](this)
      def hhw = bounds.halfWidth / 2
      ArraySeq(
        Leaf(topLeftAABB(bounds.center, hhw), thisAsParent, parameters),
        Leaf(topRightAABB(bounds.center, hhw), thisAsParent, parameters),
        Leaf(bottomLeftAABB(bounds.center, hhw), thisAsParent, parameters),
        Leaf(bottomRightAABB(bounds.center, hhw), thisAsParent, parameters)
      )
    }

    override def setChild(i: Int, c: Node[E]): Unit = children(i) = c
    override def getChild(i: Int): Node[E] = children(i)

    def findChildIndex(elem: E): Int = findQuadrant(elem.position, bounds)

    // Note: this breaks the tree structure for future additions
    def compress(): Unit = {
      children.foreach(_.compress())
      val nonEmptyChildren = children.filter(_.nonEmpty)
      children =
        if (nonEmptyChildren.size == 1 && !nonEmptyChildren.head.isLeaf)
          nonEmptyChildren.head.children
        else nonEmptyChildren
      bounds = AABB.fromPoints(children.map { _.bounds }.map { _.corners }.flatten)
    }
  }

  final class Leaf[E <: Position](
      var bounds: AABB,
      val parent: Option[Node[E]] = None,
      parameters: Parameters
  ) extends Node[E]
      with Tree.Leaf[E, Node[E]] {
    val elements = new ArrayBuffer[E](parameters.nodeElementCapacity / 4)

    def splitCondition: Boolean =
      elementCount > parameters.nodeElementCapacity && bounds.width > parameters.minNodeSize

    def toNode: Node[E] = new Branch(bounds, this.parent, parameters)

    def compress(): Unit = {
      bounds = AABB.fromPoints(elements.map { _.position })
    }
  }

}
