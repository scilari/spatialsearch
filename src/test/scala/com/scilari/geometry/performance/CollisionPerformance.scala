//package com.scilari.geometry.performance
//
//import com.scilari.geometry.collisions.Scene
//import com.scilari.geometry.models.shapes.{Polygon, RegularPolygon}
//import com.scilari.geometry.models.utils.Float2Utils
//import com.scilari.geometry.models._
//import com.scilari.geometry.plotting._
//import org.scalatest.{FlatSpec, Matchers}
//
//import scala.collection.mutable.ArrayBuffer
//
//class CollisionPerformance extends FlatSpec with Matchers {
//  // Occupy ~10% of the space with variable sized circles (sort with size and query with 2*radius)
//  // 10% of circles larger (e.g. 10X)
//  // Visualize (change color with collision)
//  // Randomly evolving movement (test also separately)
//  // Check collisions (test also separately): gather in a list and remove duplicates
//  // Non-uniform point set (following some random point etc.?)
//
//  // Compute FPS for 1k, 10k, 50k, 100k elements
//  // Find out 60 FPS element count
//
//
//  val bb: AABB = AABB.positiveSquare(750f)
//
//  val bbWithMargin: AABB = AABB.addMargin(bb, 100)
//  val n = 200 // 500
//  val timeSteps = 60*120
//  val visualize = true
//
//  val modelScale = 0.5f// 2f
//  val pentagon = RegularPolygon.Pentagon(modelScale)
//  val square = RegularPolygon.Square(modelScale * math.sqrt(2).toFloat)
//  val polygon = RegularPolygon.createModel(10, modelScale)
//
//  def createPs: Array[Body] = {
//    def radius = (5.0 + Math.random () * 10.0)/4f
//    val radii = Array.fill(n)(radius).sorted.reverse
//    radii.zipWithIndex.map{ case(r, ix) =>
//      val velBox = AABB.fromMinMax (- 120, - 120, 120, 120)
//      val tr = new Transform(
//        position = bb.randomEnclosedPoint,
//        scale = (if(ix == 0) 20 else 5)*r.toFloat,
//        rotation = (math.random()*2*Math.PI).toFloat
//      )
//      val material = Material(density = if(ix == 0) 10f else 1f)
//
//      val shape = if(ix % 7 == 6) Polygon(polygon) else if(ix % 2 == 0) Polygon(pentagon) else Polygon(square)
//      Body(shape, tr, velBox.randomEnclosedPoint, if(math.random() < 0.5) 0.01f else -0.01f,
//        static = (ix == 1), material = material)
//    }
//  }
//
//  val ps = createPs
//  val scene: Scene = new Scene(bbWithMargin, ps)
//
//  def velMove(scene: Scene) : Unit ={
//
//    //Thread.sleep(100)
//
//    scene.bodies.foreach{ body =>
//
//      val tr = body.transform
//      val p = tr.position
//      val v = body.velocity
//
//
//      if(p.x < bb.minX || p.x > bb.maxX){
//        v.x = math.signum(bb.centerX - p.x)*math.abs(body.velocity.x)
//      }
//
//      if(p.y < bb.minY || p.y > bb.maxY){
//        v.y = math.signum(bb.centerY - p.y)*math.abs(body.velocity.y)
//      }
//
//      v += Float2.randomMinusOneToOne*0.03f
//
//      val drawForce = 0.2f
//      val gravity = 0.1f
//
//
//      val leader = ps(0)
//      if(body.ix != leader.ix) {
//        val diff = (leader.position - p.position).unit * drawForce
//        v += diff
//        v += Float2Utils.down * gravity
//      } else {
//        v += Float2.randomMinusOneToOne*10f
//      }
//
//      v *= 0.9999f
//
//      body.angularVelocity *= 0.999f
//    }
//
//  }
//
//  scene.customUpdates += velMove
//
//  val renderer = new SceneRenderer(scene, bb)
//
//  val msPerFrame = (scene.dt * 1000).toInt
//  var ts: Double = System.nanoTime()/1e6
//
//  val fpss = ArrayBuffer[Double](240)
//
//  def measureFps(f: Array[Body] => Unit, name: String): Double ={
//    val fpss = ArrayBuffer[Double](60)
//    val ps = createPs
//    for(t <- 0 until timeSteps){
//      f(ps)
//      val dt = System.nanoTime()/1e6 - ts
//      fpss += 1000.0/dt
//      ts = System.nanoTime()/1e6
//    }
//    val fps = fpss.drop(60).sum/(fpss.size - 60)
//    val msPerUpdate = 1000/fps
//    println(s"$name FPS: ${fps.toInt} ($msPerUpdate)")
//    msPerUpdate
//  }
//
//  val allFps = ArrayBuffer[Double]()
//
//  var t = 0
//  while(t < timeSteps){
//    val currentTime = System.nanoTime()/1e6
//    val dt = currentTime - ts
//    if(dt > msPerFrame) {
//      val updateT0 = System.nanoTime()/1e6
//      scene.update()
//      val updateTime = System.nanoTime()/1e6 - updateT0
//      fpss += 1000.0/updateTime
//      ts = currentTime
//      t += 1
//
//      if(t != 0 && t % 240 == 0) {
//        val fps = fpss.sum/fpss.size
//        allFps += fps
//        println(s"FPS: ${fps.toInt} (${1000/fps})")
//        fpss.clear();
//      }
//
//      if(visualize){
//        renderer.update()
//      }
//    }
//
//
//  }
//
//  println(s"Average FPS: ${(allFps.sum/allFps.size).toInt}" )
//
//
//}
