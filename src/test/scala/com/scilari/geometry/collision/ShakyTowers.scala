package com.scilari.geometry.collision

import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, Float2, Shape, Transform}

import scala.util.Random

class ShakyTowers extends  CollisionBaseTest {
  val cols = 10
  val rows = 10
  val baseX = 150f
  val baseY = 300f + 200 + 10
  val dx = 25f
  val dy = 20.001f

  def random() = 0.5 - math.random()
  def smallRandom() = 1 * random()
  def largerRandom() = 5 * random()

  override def createBodies: Array[Body] = {
    val grid = for{
      i <- 0 until cols
      j <- 0 until rows
    } yield {
      Body(Polygon(tinyBox), Transform(Float2(baseX + + smallRandom() + i*dx, baseY + j*dy)))
    }

    val grid2 = for{
      i <- 0 until cols
      j <- 0 until rows
    } yield {
      Body(Polygon(tinyBox), Transform(Float2(baseX + 500 + largerRandom() + i*dx, baseY + j*dy)))
    }

    val base =  Array(
      Body(Polygon(largeBox), Transform(Float2(300f, 300f)), static = true),
      Body(Polygon(largeBox), Transform(Float2(800f, 300f)), static = true)
    )

    base ++ Random.shuffle(grid) ++ Random.shuffle(grid2)
  }



  run(Seq(gravity))

}
