package com.scilari.geometry.collision

import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, Float2, Transform}

import scala.util.Random

class FallingPyramid extends  CollisionBaseTest {
  val columns = 12
  val hugeW = 500

  val boxWidth = 25


  val hugeCenter = Float2(600f, -250f)
  val baseY = hugeCenter.y + hugeW + boxWidth
  val centerX = hugeCenter.x




  override def createBodies: Array[Body] = {


    val dx = 2*boxWidth
    val dy = 3*boxWidth

    val pyramid = for{
      col <- 0 until columns
      row <- 0 until columns - col
    } yield {
      val offSetX = centerX -columns/2 * dx + col * boxWidth
      val x = offSetX + row * dx
      val y = baseY + col * dy
      val pos = Float2(x, y)
      Body(Polygon(smallBox), Transform(pos))
    }



    val base =  Array(
      Body(Polygon(hugeBox), Transform(hugeCenter), static = true),
    )

    base ++ Random.shuffle(pyramid)
  }

  run(Seq(gravity))

}
