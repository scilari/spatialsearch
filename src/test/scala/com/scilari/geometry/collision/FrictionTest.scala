package com.scilari.geometry.collision

import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, _}

class FrictionTest extends CollisionBaseTest {


  override def createBodies = Array(
    Body(Polygon(largeBox), Transform(Float2(300, 300)), static = true),

    Body(Polygon(smallBox), Transform(Float2(150, 600)), velocity = Float2(30, 0)),

    Body(Polygon(smallBox), Transform(Float2(150, 700)), velocity = Float2(40, 0)),

    Body(Polygon(largeBox), Transform(Float2(900, 300)), static = true),

    Body(Polygon(smallBox), Transform(Float2(1150, 540)), velocity = Float2(-200, 0)),

    Body(Polygon(smallBox), Transform(Float2(1300, 400)), velocity = Float2(-100, 0)),
  )

  run(Seq(gravity))

}
