package com.scilari.geometry.collisions

import com.scilari.geometry.models.{AABB, Body, DataPoint}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree

import scala.collection.mutable.ArrayBuffer

class CollisionCollector(val objectCount: Int) {
  var handled: Array[Boolean] = Array.fill(objectCount)(false)
  var colliding: Array[Boolean] = Array.fill(objectCount)(false)
  val pairs: ArrayBuffer[(Int, Int)] = new ArrayBuffer[(Int, Int)]()

  def reset(): Unit ={
    handled = Array.fill(objectCount)(false)
    colliding = Array.fill(objectCount)(false)
    pairs.clear()
  }

  def collect(bs: Seq[Body[_]], tree: QuadTree[_]): Unit ={
    ???
//    bs.foreach{ a =>
//      if(!handled(a.ix)){
//        val candidates = tree.rangeSearch(a.shape.position, 2*a.shape.radius).map{_.data}
//        checkAndCollect(a, candidates)
//      }
//    }
  }

  private[this] def checkAndCollect(a: Body[_], candidates: Seq[Body[_]], startingIndex: Int = 0): Unit ={
    var i = startingIndex
    val n = candidates.size
    while(i < n){
//      val b = candidates(i)
//      if(a.shape.collides(b.shape)) {
//        pairs += ((a.ix, b.ix))
//        colliding(a.ix) = true
//        colliding(b.ix) = true
//        i += 1
//      }
    }
  }

//  private[this] def updateCollisionLeaves(ps: Seq[Body], tree: QuadTree[DataPoint[Body]]): Unit ={
//    val leaves = tree.root.leaves
//    leaves.foreach{ leaf =>
//      val es = leaf.elements.map{_.data}
//      val n = es.length
//      var i = 0
//      while(i < n){
//        val a = es(i)
//        val aBox = AABB(a.shape.position, 2*a.shape.radius)
//        handled(a.ix) = leaf.bounds.contains(aBox)
//        if(handled(a.ix)){
//          checkAndCollect(a, es, i + 1)
//        }
//        i += 1
//      }
//    }
//  }

}
