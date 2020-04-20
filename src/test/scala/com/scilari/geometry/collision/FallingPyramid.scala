package com.scilari.geometry.collision

import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, Float2, Transform}

import scala.util.Random

class FallingPyramid extends  CollisionBaseTest {
  val cols = 12
  val hugeW = 500
  val smallW = 25

  val hugeCenter = Float2(600f, -250f)
  val baseY = hugeCenter.y + hugeW + smallW
  val centerX = hugeCenter.x

  val dx = 2*smallW
  val dy = 3*smallW


  override def createBodies: Array[Body] = {
    val grid = for{
      col <- 0 until cols
      row <- 0 until cols - col
    } yield {
      val offSetX = centerX -cols/2 * dx + col * smallW
      val x = offSetX + row * dx
      val y = baseY + col * dy
      val pos = Float2(x, y)
      Body(Polygon(smallBox), Transform(pos))
    }

    val base =  Array(
      Body(Polygon(hugeBox), Transform(hugeCenter), static = true),
    )

    base ++ Random.shuffle(grid)
  }

  run(Seq(gravity))

}
