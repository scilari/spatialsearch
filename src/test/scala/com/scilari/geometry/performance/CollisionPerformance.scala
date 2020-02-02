package com.scilari.geometry.performance

import java.awt.{Color, Graphics2D}

import com.scilari.geometry.collisions.{CollisionCollector, SAT}
import com.scilari.geometry.models.{AABB, Body, DataPoint, Float2, HasPosition, Polygon, Transform}
import com.scilari.geometry.plotting.drawPolygon
import com.scilari.geometry.plotting._
import com.scilari.geometry.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.spatialsearch.trees.quadtree.{Parameters, QuadTree}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

class CollisionPerformance extends FlatSpec with Matchers {
  // Occupy ~10% of the space with variable sized circles (sort with size and query with 2*radius)
  // 10% of circles larger (e.g. 10X)
  // Visualize (change color with collision)
  // Randomly evolving movement (test also separately)
  // Check collisions (test also separately): gather in a list and remove duplicates
  // Non-uniform point set (following some random point etc.?)

  // Compute FPS for 1k, 10k, 50k, 100k elements
  // Find out 60 FPS element count

  val bb: AABB = AABB.positiveSquare(750f)
  val n = 200 // 500
  val timeSteps = 60*120
  val visualize = true

  val collisionCollector = new CollisionCollector(n)

  val pentagon = Polygon.Pentagon(1f)


  def createPs: Array[Body[Polygon]] = {
    def radius = (2.0 + Math.random () * 10.0)/2
    val radii = Array.fill(n)(radius).sorted.reverse
    radii.zipWithIndex.map{ case(r, ix) =>
      val velBox = AABB.fromMinMax (- 2, - 2, 2, 2)
      val tr = Transform(
        position = bb.randomEnclosedPoint,
        scale = 5*r.toFloat,
        rotation = (math.random()*2*Math.PI).toFloat
      )
      val shape = Polygon(pentagon, tr)
      Body(ix, shape, tr, velBox.randomEnclosedPoint)
    }
  }

  var ps = createPs

  def velMove(ps: Array[Body[Polygon]]): Array[Body[Polygon]] ={
    val newBodies = ps.map{_.copy()}

    newBodies.foreach{ body =>

      val tr = body.transform
      val p = tr.position
      val v = body.velocity

      p += v

      if(p.x < bb.minX || p.x > bb.maxX){
        v.x = math.signum(bb.centerX - p.x)*math.abs(body.velocity.x)
      }

      if(p.y < bb.minY || p.y > bb.maxY){
        v.y = math.signum(bb.centerY - p.y)*math.abs(body.velocity.y)
      }

      v += Float2.randomMinusOneToOne*0.03f


      val leader = ps(0)
      if(body.ix != leader.ix){
        val diff = (leader.position - p.position).unit * 0.1f
        v += diff
      } else {
        v += Float2.randomMinusOneToOne*0.2f
      }

      v *= 0.9995f

      //tr.rotation += 0.01f

      // body.shape.update()
    }
    //if(Math.random() < 0.9) return
    newBodies
  }


  def drawPoints[E <: Float2]()(g2d: Graphics2D): Unit ={
    // draw glow
    val glowColor = new Color(1f, 1f, 1f/*, 0.1f*/)
    ps.foreach { e =>
      if (collisionCollector.colliding(e.ix)) {
        drawCircle(e.position, radius = 5f, color = glowColor)(g2d)
      }
    }

    ps.foreach { e =>
      val color = if (collisionCollector.colliding(e.ix)) Color.YELLOW else Color.MAGENTA
      drawEdgeCircle(e.position, radius = 10f, faceColor = color)(g2d)
      val polyColor = if (collisionCollector.colliding(e.ix)) Color.RED else Color.GREEN
      drawPolygon(e.shape.asInstanceOf[Polygon], polyColor)(g2d)
    }

    // drawEdgeCircle(ps(0), radius = 20f, faceColor = Color.RED)(g2d)
  }

  def drawTree[E <: HasPosition](tree: QuadTree[E])(g2d: Graphics2D): Unit ={
    val nodes: Seq[AABB] = tree.root.nodes.map{_.bounds}
    nodes.foreach(b => drawAABB(b, Color.DARK_GRAY)(g2d))
  }

  val treeParams = Parameters(nodeElementCapacity = 48)

  val bbWithMargin: AABB = AABB.addMargin(bb, 100)

  var tree: QuadTree[Body[Polygon]] = QuadTree[Body[Polygon]](bbWithMargin, treeParams)

  tree.add(ps)

  val panel = new FlippedDrawingPanel(bb.width.toInt, bb.height.toInt, Color.BLACK,
    BoundedDrawingFunction(drawTree(tree)(_), () => bb),
    //BoundedDrawingFunction(drawGlow()(_), () => bb),
    BoundedDrawingFunction(drawPoints()(_), () => bb)
  )
  if (visualize) new Frame("Bouncing", panel)


  val msPerFrame = 16
  var ts: Double = System.nanoTime()/1e6


  val fpss = ArrayBuffer[Double](240)

  def measureFps(f: Array[Body[Polygon]] => Unit, name: String): Double ={
    val fpss = ArrayBuffer[Double](60)
    val ps = createPs
    for(t <- 0 until timeSteps){
      f(ps)
      val dt = System.nanoTime()/1e6 - ts
      fpss += 1000.0/dt
      ts = System.nanoTime()/1e6
    }
    val fps = fpss.drop(60).sum/(fpss.size - 60)
    val msPerUpdate = 1000/fps
    println(s"$name FPS: ${fps.toInt} ($msPerUpdate)")
    msPerUpdate
  }

  //Thread.sleep(10000)
//  def measureMove(): Double = measureFps(
//    (ps: Array[Body]) => ps.foreach(_.velMove()), "MOVE"
//  )
//
//
//  def measureMoveAndInsert(): Double = measureFps(
//    (ps: Array[VelPoint]) => {
//      tree = QuadTree[VelPoint](bbWithMargin, treeParams)
//      tree.add(ps)
//      ps.foreach(_.velMove())
//    }, "MOVE AND INSERT")
//
//  def measureAllCollisionInnerLeaf(): Double = measureFps(
//    (ps: Array[VelPoint]) => {
//      tree = QuadTree[VelPoint](bbWithMargin, treeParams)
//      tree.add(ps)
//      ps.foreach(_.velMove())
//      updateCollisionStatus(tree, ps, true)
//    }, "MOVE AND INSERT AND INLEAF")
//
//  def measureAllCollision(): Double = measureFps(
//    (ps: Array[VelPoint]) => {
//      tree = QuadTree[VelPoint](bbWithMargin, treeParams)
//      tree.add(ps)
//      ps.foreach(_.velMove())
//      updateCollisionStatus(tree, ps)
//    }, "MOVE AND INSERT AND COLLISIONS ALL")

  //measureMove()
  //measureMoveAndInsert()
  //measureAllCollisionInnerLeaf()
  //measureAllCollision()

  for(t <- 0 until timeSteps){
    ps = velMove(ps)
    tree = QuadTree[Body[Polygon]](bbWithMargin, treeParams)
    tree.add(ps)
    ps.foreach{ p =>
      val neighbors = tree.rangeSearch(p, p.shape.radius)
    }
    // updateCollisionStatus(tree, ps)


    val dt = System.nanoTime()/1e6 - ts

    if(visualize){
      val sleepTime = msPerFrame - dt
      panel.validate()
      panel.repaint()
      if(sleepTime > 0) Thread.sleep(sleepTime.toInt)
    }


    //Thread.sleep(200)
    fpss += 1000.0/dt
    if(t != 0 && t % 240 == 0) {
      val fps = fpss.sum/fpss.size
      println(s"FPS: ${fps.toInt} (${1000/fps})")
      fpss.clear();
    }

    ts = System.nanoTime()/1e6
  }
}
