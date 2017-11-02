package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait SearchTree[E <: Float2]
  extends Searches[Float2, E] with PolygonalSearches[Float2, E]
    with Searchable[E] with SpatialContainer[E] with Traversable[E] {

  val Tree: BoundedPlanarTree[E]
  var root: Tree.BaseType

  def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    val knn = new Knn(k)
    knn.search(queryPoint, root)
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E] = rangeSearch(queryPoint, r, sizeHint = 32)

  def rangeSearch(queryPoint: Float2, r: Float, sizeHint: Int): Seq[E] = {
    val range = new Range(r, sizeHint)
    range.search(queryPoint, root)
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    val knnCond = new KnnWithCondition(k, condition)
    knnCond.search(queryPoint, root)
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    val poly = new Polygonal()
    poly.search(queryPoint, root)
  }

  def polygonalMaxRangeSearch(queryPoint: Float2, maxRange: Float): Seq[E] = {
    val polyMax = new PolygonalMaxRange(maxRange)
    polyMax.search(queryPoint, root)
  }

  def polygonalDynamicMaxRangeSearch(queryPoint: Float2, maxRangeFactor: Float = 3f): Seq[E] = {
    val polyMax = new PolygonalDynamicMaxRange(maxRangeFactor)
    polyMax.search(queryPoint, root)
  }

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = {
    val rangeOrFirst = new RangeUntilFirstFound(r)
    rangeOrFirst.search(queryPoint, root).isEmpty
  }

  def add(e: E): Unit = root = root.add(e)

  def addEnclose(e: E): Unit

  def depth: Int = root.depth

  def remove(e: E): Unit = remove(Seq(e))

  def remove(queryPoint: Float2, e: E): Unit = {
    val removal = new Removal(e)
    removal.search(queryPoint, root)
  }

  override def remove(elems: Seq[E]): Unit = for(l <- root.leaves) l.elements --= elems.filter(l.contains)

  def foreach[U](f: E => U): Unit = root.foreach(f)

  override def toString(): String = root.toString()
}


