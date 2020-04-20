package com.scilari.geometry.collision

import com.scilari.geometry.collisions.Scene
import com.scilari.geometry.models.shapes.{Circle, RegularPolygon}
import com.scilari.geometry.models.utils.Float2Utils
import com.scilari.geometry.models.{AABB, Body}
import com.scilari.geometry.plotting.SceneRenderer
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

trait CollisionBaseTest extends FlatSpec with Matchers {

  val tinyBox = RegularPolygon.Square(10)
  val smallBox = RegularPolygon.Square(25)
  val mediumBox = RegularPolygon.Square(50)
  val largeBox = RegularPolygon.Square(200)
  val hugeBox = RegularPolygon.Square(500)
  val smallPentagon = RegularPolygon.Pentagon(30)

  def smallCircle: Circle = Circle(25)
  def tinyCircle: Circle = Circle(10)

  def gravity(scene: Scene) : Unit ={
    scene.bodies.foreach{ b =>
      b.velocity += Float2Utils.down * (9.81f * scene.dt)
    }
  }

  val fpsBatchSize = 1024
  val fpsForBatch = ArrayBuffer[Double]()

  def createBodies: Array[Body]

  def run(customUpdates: Seq[Scene =>  Unit] = Seq()): Unit ={
    val bodies = createBodies

    val time = 120 // s
    val bbForRenderer: AABB = AABB.positiveSquare(1200f)
    val bbForBodies: AABB = AABB.apply(bodies.map{_.position})

    val scene = new Scene(bbForBodies, bodies)
    val timeSteps = (time / scene.dt).toInt
    scene.customUpdates ++= customUpdates
    val renderer = new SceneRenderer(scene, bbForRenderer)

    val msPerFrame = (scene.dt * 1000).toInt
    var ts: Double = System.nanoTime()/1e6

    var dtForUi = -1.0

    var usedTimeForBundle: Double = 0.0

    for(t <- 0 until timeSteps){
      scene.update()
      val dt = System.nanoTime()/1e6 - ts
      val sleepTime = msPerFrame - dt
      if(dtForUi < 0.0) dtForUi = dt else dtForUi = 0.99 * dtForUi + 0.01*dt
      usedTimeForBundle += dt
      renderer.update()
      if(sleepTime > 0) Thread.sleep(sleepTime.toInt)
      ts = System.nanoTime()/1e6

      if((t + 1) % fpsBatchSize == 0) {
        val avgFps = 1000.0 * fpsBatchSize/usedTimeForBundle
        println(s"FPS: ${avgFps.toInt}")
        fpsForBatch += avgFps
        usedTimeForBundle = 0
      }

      if((t + 1) % 120 == 0) {
        renderer.debugString = Seq(
          "FPS: " + (1000.0/dtForUi).toInt,
          "Dynamic bodies: " + scene.bodies.count(b => !b.static),
          "Collisions: " + scene.collisionCollector.collisions.size
        )

      }
      // Remove bodies too far of the bounding box
      scene.bodies = scene.bodies.filter(b => bbForBodies.distance(b.position) < 500f)
    }
    println(s"Average fps: ${(fpsForBatch.sum/fpsForBatch.size).toInt}")
  }





}
