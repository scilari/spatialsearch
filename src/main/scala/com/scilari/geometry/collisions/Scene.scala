package com.scilari.geometry.collisions

import com.scilari.engine.Input
import com.scilari.geometry.models.{AABB, Body}
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree

import scala.collection.mutable.ArrayBuffer


class Scene(
  val bounds: AABB,
  var bodies: Array[Body],
  val fps: Int = 120 // TODO
) {
  val dt: Float = 1f/fps
  var tree = QuadTree[Body](bounds)
  val collisionCollector = new CollisionCollector()

  val inputHandler = new Input.Handler()
  var inputState = inputHandler.state(0)

  bodies = bodies.sortWith(_ > _)

  val customUpdates = new ArrayBuffer[Scene => Unit]()

  def update(): Unit ={
    updateShapes() // TODO: This is now in both places - fix this cleanly
    updateTree()
    updateCollisions()
    integrateVelocities()
    applyCustomUpdates()

    inputState = inputHandler.state(System.currentTimeMillis()) // TODO: make this work with ticks
  }

  private[this] def updateTree(): Unit ={
    tree = QuadTree[Body](bounds)
    bodies.foreach{tree.add}
  }

  private[this] def updateCollisions(): Unit ={
    collisionCollector.reset()
    collisionCollector.collect(tree)
    CollisionResolver.handleCollisions(collisionCollector.collisions)
  }

  private[this] def integrateVelocities(): Unit ={
    bodies.foreach(_.integrateVelocities(dt))
  }

  private[this] def updateShapes(): Unit ={
    bodies.foreach(_.shape.update())
  }

  private[this] def applyCustomUpdates(): Unit ={
    customUpdates.foreach { _(this) }
  }

  // TODO: gravity, drag? Or should these be applied to per body basis (e.g. air vs water)
}

