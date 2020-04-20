package com.scilari.geometry.collision


import com.scilari.geometry.collisions.Scene
import com.scilari.geometry.models.shapes.{Circle, Polygon, Segment}
import com.scilari.geometry.models.{Body, Float2, Material, Transform}
import com.scilari.math.ArrayUtils
import com.scilari.math.Pi

import scala.util.Random

class GroundTest extends CollisionBaseTest {
  val cols = 12
  val hugeW = 500
  val smallW = 25

  val hugeCenter = Float2(600f, -250f)
  val baseY = hugeCenter.y + hugeW + smallW
  val centerX = hugeCenter.x

  val dx = 2*smallW
  val dy = 3*smallW

  val rotatingBall = Body(Circle(20f), Transform(Float2(150, 600)), material = Material.LEAD)

  def controlBall(scene: Scene) : Unit ={
    rotatingBall.angularVelocity = -20f
  }


  override def createBodies: Array[Body] = {

    println(rotatingBall.mass)

    val grid = for{
      col <- 0 until cols
      row <- 0 until cols - col
    } yield {
      val offSetX = centerX -cols/2 * dx + col * smallW
      val x = offSetX + row * dx
      val y = baseY + col * dy
      val pos = Float2(x, y)
      Body(Polygon(tinyBox), Transform(pos), material = Material.WOOD)
    }

    println(grid(0).mass)

    val base = {
      val r = 500
      val center = Float2(600, 600)
      val alphas = ArrayUtils.linSpace(0.5f*Pi, 2.5f*Pi, 200)
      val polyline = alphas.map{ a => center + Float2(math.cos(a), math.sin(a)) * r }
      Segment.bodiesFromPolyLine(polyline, Material.WOOD)
    }.toArray


    base ++
      Random.shuffle(grid) ++
      Array(rotatingBall)
  }

  run(Seq(gravity, controlBall))

}