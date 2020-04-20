package com.scilari.geometry.collisions

import com.scilari.geometry.models.{AABB, Body, Shape}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTreeLike.QuadNode

import scala.collection.mutable.ArrayBuffer

class CollisionCollector() {
  val collisions: ArrayBuffer[Collision] = new ArrayBuffer[Collision]()

  def reset(): Unit ={
    collisions.clear()
  }

  def collect(tree: QuadTree[Body]): Unit ={
    def possibleCollision(b1: Body, b2: Body): Boolean = {
      !(b1.static && b2.static) && {
        val s1 = b1.shape
        val s2 = b2.shape
        val r = s1.radius + s2.radius
        s1.position.distanceSq(s2.position) < r*r
      }
    }

    tree.root.leaves.foreach{ leaf =>
      val es = leaf.elements
      val n = es.length
      var i = 0
      while(i < n) {
        val e1 = es(i)
        var j = i + 1
        while(j < n) {
          val e2 = es(j)
          if(possibleCollision(e1, e2)) collect(e1.computeCollision(e2))
          j += 1
        }

        // Have to use 2*r in both places to ensure that the larger will find the smaller always
        // TODO: think this still: maybe possible to use tighter limits here
        val twoR = 2*e1.shape.radius
        val bb = AABB(e1.position, twoR)
        if (!leaf.bounds.contains(bb)) {
          val neighbors = tree.rangeExcludeNode(e1.position, twoR, leaf)
          neighbors.foreach{ e2 =>
            if (possibleCollision(e1, e2)) {
              collect(e1.computeCollision(e2))
            }
          }
        }
        i += 1
      }
    }

  }

  private[this] def collect(collisionOption: Option[Collision]): Unit = {
    collisionOption.foreach { collision =>
      collisions += collision
    }
  }





}
