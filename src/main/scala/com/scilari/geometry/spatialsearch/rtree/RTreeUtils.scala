package com.scilari.geometry.spatialsearch.rtree

import com.scilari.geometry.models.{AABB, Float2}

import scala.collection.mutable.ListBuffer

object RTreeUtils{

  // From paper Ang and Tan "New Linear Node Splitting Algorithm for R-trees" (with slight modifications)
  def angLinearSplit(node: AABB, points: Seq[Float2]): (AABB, AABB) ={
    // Using point center instead of node center (somewhat improves query performance)
    val xs = points.map{_.x}
    val ys = points.map{_.y}
    //val centerX = (xs.max + xs.min)/2
    //val centerY = (ys.max + ys.min)/2

    val centerX = node.centerX
    val centerY = node.centerY

    // arranging into four lists based on the side they are closest to
    val (right: ListBuffer[Float2], left: ListBuffer[Float2]) = points.partition(_.x > centerX)
    val (top: ListBuffer[Float2], bottom: ListBuffer[Float2]) = points.partition(_.y > centerY)

    // deciding along which dimension to split
    val diffX = math.abs(left.size - right.size)
    val diffY = math.abs(bottom.size - top.size)


    val splitX = if(diffX == diffY){
      variance(xs) > variance(ys) // tie breaker
    }  else {
      diffX < diffY
    }

    // finding the extreme points along the selected dimension
    val (minPoint, maxPoint) = if(splitX) {
      (points.minBy(_.x), points.maxBy(_.x))
    } else {
      (points.minBy(_.y), points.maxBy(_.y))
    }

    // choose which lists to use based on the dimension and filter extreme points
    val minList = (if(splitX) left else bottom).filter(p => p != minPoint && p != maxPoint)
    val maxList = (if(splitX) right else top).filter(p => p != minPoint && p != maxPoint)

    val minAABB = AABB(minPoint, minPoint)
    val maxAABB = AABB(maxPoint, maxPoint)

    // add from the bigger list first to keep the MBRs similarly sized
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

    // Handling the first expansion separately to avoid some situations, where candidate is very narrow and area close
    // to zero.
    val areaA = if(boxA.area != 0) {
      candidateA.area
    } else {
      val m = (candidateA.width + candidateA.height)/2
      m*m
    }

    val areaB = if(boxB.area != 0) {
      candidateB.area
    } else {
      val m = (candidateB.width + candidateB.height)/2
      m*m
    }

    areaA - boxA.area < areaB - boxB.area
  }

  def variance(xs: Seq[Float]): Float = {
    val mean = xs.sum/xs.size
    xs.map{ x =>
      val dx = mean - x
      dx*dx
    }.sum/xs.size
  }



}
