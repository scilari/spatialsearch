package com.scilari.geometry.collision


import com.scilari.geometry.collisions.Scene
import com.scilari.geometry.models
import com.scilari.geometry.models.shapes.{Circle, Polygon, Segment}
import com.scilari.geometry.models.utils.Float2Utils
import com.scilari.geometry.models.{AABB, Body, Float2, Material, Transform}
import com.scilari.math.ArrayUtils
import com.scilari.math.Pi

import scala.util.Random

class GameScene extends CollisionBaseTest {
  override val bbForRenderer = GameScene.bounds

  val player = Body(Circle(1.0f), Transform(GameScene.bounds.center), material = Material.PLAYER)
  val ball = Body(Circle(2.0f), Transform(GameScene.bounds.center - Float2(0, 4)), material = Material.BALLOON)

  var playerRotation = 20f

  var tuningDir: Float2 = Float2(0, 0)

  val resetArea: AABB = AABB(GameScene.bounds.relativeCoordinates(0.5f, 0.9f), GameScene.width/20)

  def controlBall(scene: Scene) : Unit ={
    val nearestStatic = scene.tree.knnSearchWithFilter(player.position, 1, (b: Body) => b.static).headOption
    nearestStatic.foreach { nn =>
      val dir = nn.position - player.position

      val normal = nn.shape.normalInDirection(dir)
      val tuneFactor = if(dir.lengthSq < 10 * 10) 0.99f else 0.999f
      tuningDir = tuningDir*tuneFactor + normal.perpCCW*(1f - tuneFactor)
      player.transform.rotation = tuningDir.direction


      player.angularVelocity = playerRotation
      scene.inputState.keysReleased.foreach{ case(_, k) =>
        val dirChangeThreshold = 200
        if(k.durationTicks < dirChangeThreshold) {
          playerRotation *= -1f
        } else {
          val velChange = 30 * player.radius * math.min(2f, (k.durationTicks - dirChangeThreshold)/1000f)
          player.velocity -= normal * velChange
          //rotatingBall.applyImpulse(dir.normalized * impulseStrength, rotatingBall.position)
          println("JUMP" + dir.direction + " " + k.durationTicks/1000f)
        }
      }
    }

    scene.bodies.filter(b => !b.static && !GameScene.bounds.contains(b.position)).foreach{ b =>
      b.transform.position = resetArea.randomEnclosedPoint
      b.velocity *= 0.5f
      //b.angularVelocity = 0f
    }



  }


  override def createBodies: Array[Body] = {
    GameScene.Borders.all ++ GameScene.Platforms.all ++ Seq(player, ball)
  }

  run(Seq(gravity, controlBall))

}

object GameScene {
  val width = 80f
  val height = 45f
  val bounds: AABB = AABB(Float2(width/2, height/2), width/2, height/2)

  object Borders {
    def arc(center: Float2, radius: Float, sectorStart: Float, sectorEnd: Float, points: Int = 50): Seq[Body] = {
      val alphas = ArrayUtils.linSpace(sectorStart, sectorEnd, points)
      val polyline = alphas.map{ a => center + Float2(math.cos(a), math.sin(a)) * radius }
      Segment.bodiesFromPolyLine(polyline, Material.WOOD)
    }

    val offset = 0.3f * height
    val r = 0.25f * height

    val bottomLeftCorner = arc(bounds.bottomLeft + Float2(offset), r, 1f*Pi, 1.5f*Pi)
    val bottomRightCorner = arc(bounds.bottomRight + Float2(-offset, offset), r, 1.5f*Pi, 2f*Pi)
    val topLeftCorner = arc(bounds.topLeft + Float2(offset, -offset), r, 0.5f*Pi, 1f*Pi)
    val topRightCorner = arc(bounds.topRight + Float2(-offset, -offset), r, 0f*Pi, 0.5f*Pi)

    val floorCeilingLength = 22f
    val leftFloor = Segment.bodiesFromPolyLine(Float2Utils.linSpace(
      bounds.bottomLeft + Float2(offset, offset - r),
      bounds.bottomLeft + Float2(offset, offset - r) + Float2Utils.right * floorCeilingLength, 5), Material.WOOD)

    val rightFloor = Segment.bodiesFromPolyLine(Float2Utils.linSpace(
      bounds.bottomRight + Float2(-offset, offset - r),
      bounds.bottomRight + Float2(-offset, offset - r) + Float2Utils.left * floorCeilingLength, 5), Material.WOOD)

    val leftCeiling = Segment.bodiesFromPolyLine(Float2Utils.linSpace(
      bounds.topLeft + Float2(offset, -(offset - r)),
      bounds.topLeft + Float2(offset, -(offset - r)) + Float2Utils.right * floorCeilingLength, 5), Material.WOOD)

    val rightCeiling = Segment.bodiesFromPolyLine(Float2Utils.linSpace(
      bounds.topRight + Float2(-offset, -(offset - r)),
      bounds.topRight + Float2(-offset, -(offset - r)) + Float2Utils.left * floorCeilingLength, 5), Material.WOOD)



    def all: Array[Body] = (
      bottomLeftCorner ++ bottomRightCorner ++ topLeftCorner ++ topRightCorner ++
      leftFloor ++ rightFloor ++ leftCeiling ++ rightCeiling

      ).toArray
  }

  object Platforms {
    def wideBase: Polygon = Polygon.fromAABB(AABB(Float2.zero, width/8, height/60))
    def thinBase: Polygon = Polygon.fromAABB(AABB(Float2.zero, width/12, height/60))

    val bottom: Body = Body(wideBase, Transform(bounds.relativeCoordinates(0.5f, 0.25f)), static = true)
    val top: Body = Body(wideBase, Transform(bounds.relativeCoordinates(0.5f, 0.6f)), static = true)
    val left: Body = Body(thinBase, Transform(bounds.relativeCoordinates(0.2f, 0.4f)), static = true)
    val right: Body = Body(thinBase, Transform(bounds.relativeCoordinates(0.8f, 0.4f)), static = true)

    def all: Array[Body] = Array(bottom, top, left, right)

  }




}
