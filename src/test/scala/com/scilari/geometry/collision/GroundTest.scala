package com.scilari.geometry.collision


import com.scilari.geometry.collisions.Scene
import com.scilari.geometry.models.shapes.{Circle, Polygon, Segment}
import com.scilari.geometry.models.{Body, Float2, Material, Transform}
import com.scilari.math.ArrayUtils
import com.scilari.math.Pi

import scala.util.Random

class GroundTest extends CollisionBaseTest {
  val cols = 12
  val hugeW = hugeBox.radius/math.sqrt(2)
  val smallW = smallBox.radius/math.sqrt(2)

  val hugeCenter = Float2(60f, -25f)
  val baseY = hugeCenter.y + hugeW + smallW
  val centerX = hugeCenter.x

  val dx = 2*smallW
  val dy = 3*smallW


  val rotatingBall = Body(Circle(2f), Transform(hugeCenter + Float2(-10, 2*hugeW)), material = Material.LEAD)

  var ballRotation = -25f

  var tuningDir: Float2 = Float2(0, 0)

  def controlBall(scene: Scene) : Unit ={
    val nearestStatic = scene.tree.knnSearchWithFilter(rotatingBall.position, 1, (b: Body) => b.static).headOption
    nearestStatic.foreach { nn =>
      val dir = (nn.position - rotatingBall.position).normalized

      tuningDir = tuningDir*0.95f + dir.perpCCW*0.05f
      rotatingBall.transform.rotation = tuningDir.direction


      rotatingBall.angularVelocity = ballRotation
      scene.inputState.keysReleased.foreach{ case(_, k) =>
        if(k.durationTicks < 200) {
          ballRotation *= -1f
        } else {
          val velChange = 20 * rotatingBall.radius * math.min(3f, k.durationTicks/1000f)
          rotatingBall.velocity -= dir * velChange
          //rotatingBall.applyImpulse(dir.normalized * impulseStrength, rotatingBall.position)
          println("JUMP" + dir.direction + " " + k.durationTicks/1000f)
        }
      }
    }





  }


  override def createBodies: Array[Body] = {

    println(rotatingBall.mass)

    val grid = for{
      col <- 0 until cols
      row <- 0 until cols - col
    } yield {
      val offSetX = centerX -cols/2f * dx + col * smallW
      val x = offSetX + row * dx
      val y = baseY + col * dy
      val pos = Float2(x, y)
      Body(Polygon(tinyBox), Transform(pos), material = Material.WOOD)
    }

    println(grid(0).mass)

    val base = {
      val r = 40
      val center = Float2(hugeCenter.x, 40f)
      val alphas = ArrayUtils.linSpace(0.5f*Pi, 2.5f*Pi, 500)
      val polyline = alphas.map{ a => center + Float2(math.cos(a), math.sin(a)) * r }
      Segment.bodiesFromPolyLine(polyline, Material.WOOD)
    }.toArray


    base ++
      Random.shuffle(grid) ++
      Array(rotatingBall)
  }

  run(Seq(gravity, controlBall))

}