package com.scilari.geometry.collisions

import com.scilari.geometry.models.{AABB, Body, Float2, Polygon}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree

import scala.collection.mutable.ArrayBuffer

class CollisionCollector(val objectCount: Int) {
  type B = Body[Polygon]
  var handled: Array[Boolean] = Array.fill(objectCount)(false)
  var colliding: Array[Boolean] = Array.fill(objectCount)(false)
  val collisions: ArrayBuffer[Collision[Polygon, Polygon]] = new ArrayBuffer[Collision[Polygon, Polygon]]()

  def reset(): Unit ={
    handled = Array.fill(objectCount)(false)
    colliding = Array.fill(objectCount)(false)
    collisions.clear()
  }

  def collect(bs: Seq[B], tree: QuadTree[B]): Unit ={
    val unhandled = new ArrayBuffer[Body[Polygon]]()

    tree.root.leaves.foreach{ leaf =>
      val es = leaf.elements
      val n = es.length
      var i = 0
      while(i < n) {
        val e1 = es(i)
        var j = i + 1
        while(j < n) {
          val e2 = es(j)
          collect(SAT.computeCollision(e1, e2))
          j += 1
        }

        val bb = AABB(e1.position, e1.shape.radius)
        if (!leaf.bounds.contains(bb)) unhandled += e1
        i += 1
      }
    }

    unhandled.foreach{ p =>
      val neighbors = tree.rangeExcludeNode(p.position, 2*p.shape.radius)
      neighbors.foreach{ n =>
        collect(SAT.computeCollision(p, n))
      }
    }
  }

  private[this] def collect(collisionOption: Option[Collision[Polygon, Polygon]]): Unit = {
    collisionOption match {
      case Some(collision) =>
        colliding(collision.a.ix) = true
        colliding(collision.b.ix) = true
        collisions += collision
      case None => ()
    }
  }

  def handleCollisions(): Unit = {
    collisions.foreach { c =>
      handleCollision(c)
    }
  }

  def handleCollision(collision: Collision[Polygon, Polygon]): Unit = {
    val a = collision.a
    val b = collision.b
    val invMassA = 1f/(a.shape.radius * a.shape.radius) * (if (a.ix == 0) 0f else 1f)
    val invMassB = 1f/(b.shape.radius * b.shape.radius) * (if (b.ix == 0) 0f else 1f)
    val restitutionA = 0.8f
    val restitutionB = 0.8f
    val rv: Float2 = b.velocity - a.velocity
    val normal = collision.contact.normal // don't compute actual normal at this point
    val velNormal = rv.dot(normal)
    if(velNormal <= 0) {
      val e =  math.min(restitutionA, restitutionB)
      val j = -(1f + e)*velNormal / (invMassA + invMassB)
      val impulse = normal * j
      a.velocity -= impulse * invMassA
      b.velocity += impulse * invMassB
    }

    val penetration = collision.contact.penetration
    val tolerance = 0.01f
    if(penetration > tolerance * a.shape.radius) {
      val correctionRatio = 0.75f
      val correction = correctionRatio * penetration
      val ca = invMassA / (invMassA + invMassB)
      val cb = 1f - ca
      a.position -= normal * ca * correction
      b.position += normal * cb * correction
    }

  }


}
