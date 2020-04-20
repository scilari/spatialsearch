package com.scilari.geometry.collision

import com.scilari.geometry.models.shapes.Polygon
import com.scilari.geometry.models.{Body, _}

class StackingTest extends CollisionBaseTest {


  override def createBodies: Array[Body] = Array(
    Body(Polygon(largeBox), Transform(Float2(300, 300)), static = true),

    // small falling stack
    Body(Polygon(smallBox), Transform(Float2(125, 600))),
    Body(Polygon(smallBox), Transform(Float2(125, 700))),

    // second stable stack
//    Body(Polygon(smallBox), Transform(Float2(200, 525))),
//    Body(Polygon(smallBox), Transform(Float2(200, 575))),
//    Body(Polygon(smallBox), Transform(Float2(200, 625))),
//    Body(Polygon(smallBox), Transform(Float2(200, 675))),
//    Body(Polygon(smallBox), Transform(Float2(200, 725))),
//    Body(Polygon(smallBox), Transform(Float2(200, 775))),

    Body(smallCircle, Transform(Float2(200, 525))),
    Body(smallCircle, Transform(Float2(200, 575))),
    Body(smallCircle, Transform(Float2(200, 625))),
    Body(smallCircle, Transform(Float2(200, 675))),
    Body(smallCircle, Transform(Float2(200, 725))),
    Body(smallCircle, Transform(Float2(200, 775))),


    // stable stack with small offsets
    Body(Polygon(smallBox), Transform(Float2(300, 525))),
    Body(Polygon(smallBox), Transform(Float2(300 + 10, 575))),
    Body(Polygon(smallBox), Transform(Float2(300, 625))),
    Body(Polygon(smallBox), Transform(Float2(300 -10, 675))),
    Body(Polygon(smallBox), Transform(Float2(300, 725))),
    Body(Polygon(smallBox), Transform(Float2(300, 775))),


    // balancing stack
    Body(Polygon(smallBox), Transform(Float2(400, 625))),
    Body(Polygon(smallBox), Transform(Float2(400 -25f, 575))),
    Body(Polygon(smallBox), Transform(Float2(400 +25f, 575))),
    Body(Polygon(smallBox), Transform(Float2(400, 525))),

    // falling stack
    Body(Polygon(smallBox), Transform(Float2(500, 600))),
    Body(Polygon(smallBox), Transform(Float2(505, 700))),

    // bullet breaking them all
    Body(Polygon(smallBox), Transform(Float2(2000, 700)), velocity = Float2(-500, 0)),


  )

  run(Seq(gravity))

}