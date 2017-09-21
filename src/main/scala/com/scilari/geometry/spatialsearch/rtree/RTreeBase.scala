package com.scilari.geometry.spatialsearch.rtree

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.Tree
import com.scilari.geometry.spatialsearch.Tree.{Leaf, Node}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

abstract class RTreeBase [E <: Float2](bb: AABB)
  extends AABB(bb) with Tree[Float2, E] {
  val parent: RTreeNode[E]
  def add(elem: E): RTreeBase[E]
}

class RTreeNode[E <: Float2](
  bb: AABB,
  elems: Seq[E],
  val parent: RTreeNode[E] = null
) extends RTreeBase[E](bb) with Node[Float2, E, RTreeBase[E]]{

  val (boxA, boxB) = RTreeUtils.angLinearSplit(this, elems)
  var childA: RTreeBase[E] = new RTreeLeaf[E](boxA, this)
  var childB: RTreeBase[E] = new RTreeLeaf[E](boxB, this)

  elems.foreach { e =>
    if(childA.contains(e)) childA.add(e) else childB.add(e)
  }


  def children: Seq[RTreeBase[E]] =  Seq[RTreeBase[E]](childA, childB)

  def add(elem: E): RTreeNode[E] = {
    if(RTreeUtils.isEnclosingFirst(childA, childB, elem))
      childA = childA.add(elem)
    else
      childB = childB.add(elem)

    this
  }


}

class RTreeLeaf[E <: Float2](
  bb: AABB,
  val parent: RTreeNode[E] = null
) extends RTreeBase[E](bb) with Leaf[Float2, E] {

  val nodeElementCapacity = 16
  val children: mutable.Buffer[E] = ListBuffer[E]()

  def add(elem: E): RTreeBase[E] = {
    enclose(elem)
    children += elem
    if(children.size > nodeElementCapacity) split() else this
  }

  def split(): RTreeNode[E] = {
    val newParent = new RTreeNode[E](this, children)
    newParent
  }



}

object RTreeUtils{

  def angLinearSplit(node: AABB, points: Seq[Float2]): (AABB, AABB) ={
    // working lists
    val top = ArrayBuffer.empty[Float2]
    val bottom = ArrayBuffer.empty[Float2]
    val left= ArrayBuffer.empty[Float2]
    val right = ArrayBuffer.empty[Float2]

    val centerX = node.centerX
    val centerY = node.centerY

    // arranging into four lists based on the side they are closest to
    points.foreach{ p =>
      if(p.x > centerX) right += p else left += p
      if(p.y > centerY) top += p else bottom += p
    }

    // deciding along which dimension to split
    val splitX = math.abs(left.size - right.size) < math.abs(bottom.size - top.size)

    // finding the extreme points along the selected dimension
    val (minPoint, maxPoint) = if(splitX){
      (
        if(left.nonEmpty) left.minBy(_.x) else points.minBy(_.x),
        if(right.nonEmpty) right.maxBy(_.x) else points.maxBy(_.x)
      )
    } else {
      (
        if(bottom.nonEmpty) bottom.minBy(_.y) else points.minBy(_.y),
        if(top.nonEmpty) top.maxBy(_.y) else points.maxBy(_.y)
      )
    }

    // choose which lists to use based on the dimension and filter extreme points
    val minList = (if(splitX) left else bottom).filter(p => p != minPoint && p != maxPoint)
    val maxList = (if(splitX) right else top).filter(p => p != minPoint && p != maxPoint)

    val minAABB = AABB(minPoint, minPoint)
    val maxAABB = AABB(maxPoint, maxPoint)

    // add from the bigger list first
    while(minList.nonEmpty || maxList.nonEmpty){
      val list = if(minList.size > maxList.size) minList else maxList
      val p = list.last
      list.trimEnd(1)
      chooseEnclosing(minAABB, maxAABB, p).enclose(p)
    }

    (minAABB, maxAABB)

  }

  def chooseEnclosing[B <: AABB](boxA: B, boxB: B, point: Float2): AABB ={
    if(isEnclosingFirst(boxA, boxB, point)) boxA else boxB
  }

  def isEnclosingFirst(boxA: AABB, boxB: AABB, point: Float2): Boolean = {
    val candidateA = AABB(boxA).enclose(point)
    val candidateB = AABB(boxB).enclose(point)
    candidateA.area - boxA.area < candidateB.area - boxB.area
  }





}
