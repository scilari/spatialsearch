package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.{AABB, Float2}
import QuadTreeUtils._

/**
  * Created by iv on 1/17/2017.
  */
class QuadTreeEntry[T <: Float2](bb: AABB = AABB.unit) extends SearchTree[T] {
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

  def fitToElements(elems: Seq[T]): QuadTreeEntry[T] = {
    root = new QuadNode[T](AABB.Square(elems))
    elems.foreach(add)
    this
  }

  def isEmpty = root.isEmpty

  def knnSearch(queryPoint: Float2, k: Int): Seq[T] = {
    val knn = new Searches.Knn[Float2, QuadTree[T], T] (k)
    knn.search(queryPoint, root)
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[T] = {
    val range = new Searches.Range[Float2, QuadTree[T], T](r)
    range.search(queryPoint, root)
  }

  override def polygonalSearch(queryPoint: Float2) = {
    val poly = new Searches.Polygonal[T]()
    poly.search(queryPoint, root)
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: T => Boolean): Seq[T] = {
    val knnCond = new Searches.KnnWithCondition[Float2, QuadTree[T], T](k, condition)
    knnCond.search(queryPoint, root)
  }

}

object QuadTreeEntry{
  def apply[T <: Float2](bb: AABB = AABB.unit) = new QuadTreeEntry[T](bb)
  def apply[T <: Float2](elems: Seq[T]): QuadTreeEntry[T] = {
    val q = QuadTreeEntry[T]()
    q.fitToElements(elems)
  }


}
