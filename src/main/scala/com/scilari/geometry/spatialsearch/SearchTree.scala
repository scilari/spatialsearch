package com.scilari.geometry.spatialsearch

import com.scilari.geometry.models.Float2

trait SearchTree[E <: Float2]
  extends Searches[Float2, E] with PolygonalSearches[Float2, E]
    with Searchable[E] with SpatialContainer[E] with Traversable[E] {

  val Tree: BoundedPlanarTree[E]
  var root: Tree.BaseType

  def knnSearch(queryPoint: Float2, k: Int): Seq[E] = {
    new Knn(k).search(queryPoint, root)
  }

  def rangeSearch(queryPoint: Float2, r: Float): Seq[E] =
    rangeSearch(queryPoint, r, sizeHint = Searches.defaultRangeSizeHint)

  def rangeSearch(queryPoint: Float2, r: Float, sizeHint: Int = Searches.defaultRangeSizeHint): Seq[E] = {
    new Range(r, sizeHint).search(queryPoint, root)
  }

  def knnSearchWithCondition(queryPoint: Float2, k: Int, condition: E => Boolean): Seq[E] = {
    new KnnWithCondition(k, condition).search(queryPoint, root)
  }

  override def polygonalSearch(queryPoint: Float2): Seq[E] = {
    new Polygonal().search(queryPoint, root)
  }

  def polygonalMaxRangeSearch(queryPoint: Float2, maxRange: Float): Seq[E] = {
    new PolygonalMaxRange(maxRange).search(queryPoint, root)
  }

  def polygonalDynamicMaxRangeSearch(queryPoint: Float2, maxRangeFactor: Float = 3f): Seq[E] = {
    new PolygonalDynamicMaxRange(maxRangeFactor).search(queryPoint, root)
  }

  override def isEmptyRange(queryPoint: Float2, r: Float): Boolean = {
    new RangeUntilFirstFound(r).search(queryPoint, root).isEmpty
  }

  def add(e: E): Unit = root = root.add(e)

  def addEnclose(e: E): Unit

  def depth: Int = root.depth

  def remove(e: E): Unit = remove(e, e)

  def remove(queryPoint: Float2, e: E): Unit = {
    val removal = new Removal(e)
    removal.search(queryPoint, root)
  }

  override def remove(elems: Seq[E]): Unit = for(l <- root.leaves) l.elements --= elems.filter(l.contains)

  def foreach[U](f: E => U): Unit = root.foreach(f)

  override def toString(): String = root.toString()

  def elements: Seq[E] = root.elements
}


