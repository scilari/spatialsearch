package com.scilari.geometry.collision

import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, _}

class SimpleTest extends CollisionBaseTest {

  override def createBodies: Array[Body] = Array(
    Body(Polygon(largeBox), Transform(Float2(500, 300)), static = true),

    // straight falling box
    //Body(Polygon(mediumBox), Transform(Float2(400, 700))),

    // about diagonally falling box
    Body(Polygon(mediumBox), Transform(Float2(600, 700), rotation = (Math.PI/4).toFloat + 0.1f)),
  )

  run(Seq(gravity))

}