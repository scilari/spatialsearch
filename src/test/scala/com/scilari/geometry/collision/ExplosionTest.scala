package com.scilari.geometry.collision

import com.scilari.geometry.collisions.Scene
import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, Float2, Shape, Transform}

import scala.util.Random

class ExplosionTest extends  CollisionBaseTest {
  val cols = 10
  val rows = 100
  val baseX = 150f
  val baseY = 530f
  val dx = 25f
  val dy = 20.001f



  override def createBodies: Array[Body] = {
    val grid = for{
      i <- 0 until cols
      j <- 0 until rows
    } yield {
      Body(Polygon(tinyBox), Transform(Float2(baseX + i*dx, baseY + j*dy)))
    }

    val grid2 = for{
      i <- 0 until cols
      j <- 0 until rows
    } yield {
      //if(j % 2 == 0)
        //Body(tinyCircle, Transform(Float2(baseX + 500 + i*dx, baseY + j*dy)))
      //else
        Body(Polygon(tinyBox), Transform(Float2(baseX + 500 + i*dx, baseY + j*dy)))
    }

    val base =  Array(
      Body(Polygon(largeBox), Transform(Float2(300f, 300f)), static = true),
      Body(Polygon(largeBox), Transform(Float2(800f, 300f)), static = true)
    )

    println("Grid size: " + grid.size)

    base ++ Random.shuffle(grid) ++ Random.shuffle(grid2)
  }

  var timer = 0

  def explosion(scene: Scene) : Unit ={
    //rotatingBody.angularVelocity = -5f
    //scene.bodies.foreach(_.angularVelocity += (math.random().toFloat - 0.5f)*0.1f)
    val blastOff = 2000

    if(timer == blastOff) {
      println("BOOM!")
      val explosionCenter = Float2(baseX + dx * cols/2, baseY)
      scene.bodies.foreach{ b =>

        if(b.position.distance(explosionCenter) < 400f){
          val dir = b.position - explosionCenter
          val impulse = dir.normalize() * 50000f
          b.applyImpulse(impulse, b.position + Float2.randomMinusOneToOne * 2)
        }
      }
    }

    if(timer == blastOff + 300) {
      println("BOOM 2!")
      val explosionCenter = Float2(baseX + 500 + dx * cols/2, baseY)
      scene.bodies.foreach{ b =>
        if(b.position.distance(explosionCenter) < 400f){
          val dir = b.position - explosionCenter
          val impulse = dir.normalize() * 1.5f*50000f
          b.applyImpulse(impulse, b.position + Float2.randomMinusOneToOne * 2)
        }
      }
    }

    if(timer % 100 == 0) println("Timer: " + timer)
    timer += 1


  }

  run(Seq(explosion, gravity))

}
