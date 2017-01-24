package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch._
import com.scilari.geometry.spatialsearch.quadtree.QuadTreeUtils._

/**
  * Concrete QuadTree implementation of SearchTree
  * @param bb Initial bounding box describing the root
  * @tparam T Element type
  */
class QuadTreeEntry[T <: Float2] private (bb: AABB) extends SearchTree[T] {
  var root: QuadTree[T] = new QuadLeaf[T](bb)

  def add(elem: T): Unit = root = root.add(elem)

  def addEnclose(elem: T): Unit = {
    if(root.contains(elem))
      add(elem)
    else{
      val newAABB = enclosingAABB(elem, root)
      val newRoot = new QuadNode[T](newAABB)
      newRoot.setChild(findQuadrant(root.center, newRoot), root)
      root = newRoot
      addEnclose(elem)
    }
  }

  def isEmpty: Boolean = root.isEmpty

  def knnSearch(queryPoint: Float2, k: Int): Seq[T] = {
    val knn = new Searches.Knn[Float2, QuadTree[T], T] (k)
    knn.search(queryPoint, root)
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[T] = {
    val range = new Searches.Range[Float2, QuadTree[T], T](r)
    range.search(queryPoint, root)
  }

  override def polygonalSearch(queryPoint: Float2): Seq[T] = {
    val poly = new Searches.Polygonal[T]()
    poly.search(queryPoint, root)
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: T => Boolean): Seq[T] = {
    val knnCond = new Searches.KnnWithCondition[Float2, QuadTree[T], T](k, condition)
    knnCond.search(queryPoint, root)
  }

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = {
    val rangeOrFirst = new Searches.RangeUntilFirstFound[Float2, QuadTree[T], T](r)
    rangeOrFirst.search(queryPoint, root).isEmpty
  }

}

object QuadTreeEntry{

  def apply[T <: Float2](bb: AABB = AABB.unit) = new QuadTreeEntry[T](bb)

  def apply[T <: Float2](elems: Seq[T]): QuadTreeEntry[T] = {
    require(elems.size > 1, "At least two elements required for creating the initial node.")
    val square = AABB.EnclosingSquare(elems)
    require(square.width > 0 && square.height > 0,
      "At least two spatially distinct elements required for creating the initial node.")
    val q = QuadTreeEntry[T](square)
    elems.foreach(q.add)
    q
  }


}
