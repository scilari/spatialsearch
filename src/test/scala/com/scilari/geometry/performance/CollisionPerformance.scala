//package com.scilari.geometry.performance
//
//import java.awt.{Color, Graphics2D}
//
//import com.scilari.geometry.collisions.{CollisionCollector, SAT}
//import com.scilari.geometry.models.{AABB, Body, DataPoint, Float2, Polygon}
//import com.scilari.geometry.plotting.drawPolygon
//import com.scilari.geometry.plotting._
//import com.scilari.geometry.plotting.Panels.{FlippedDrawingPanel, Frame}
//import com.scilari.geometry.spatialsearch.trees.quadtree.{Parameters, QuadTree}
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
//  val bb: AABB = AABB.positiveSquare(750f)
//  val n = 200 // 500
//  val timeSteps = 60*120
//  val visualize = true
//
//  val collisionCollector = new CollisionCollector(n)
//
//  val pentagon = Polygon.Pentagon(1f)
//
//
//
//
//
//
//  def createPs: Array[Body] = {
//    def radius = (2.0 + Math.random () * 10.0)/2
//    val radii = Array.fill(n)(radius).sorted.reverse
//    radii.zipWithIndex.map{ case(r, ix) =>
//      val velBox = AABB.fromMinMax (- 2, - 2, 2, 2)
//      val shape = Polygon(
//        pentagon, position = bb.randomEnclosedPoint,
//        scale = 5*r.toFloat,
//        rotation = (math.random()*2*Math.PI).toFloat
//      )
//      Body(ix, shape, velBox.randomEnclosedPoint)
//    }
//  }
//
//  val ps = createPs
//
//  def velMove(): Unit ={
//    ps.foreach{ p =>
//      p.shape = p.shape.asInstanceOf[Polygon].move(p.v)
//      // p.shape.position += p.v
//
//      if(p.shape.position.x < bb.minX || p.shape.position.x > bb.maxX){
//        p.v.x = math.signum(bb.centerX - p.shape.position.x)*math.abs(p.shape.position.x)
//      }
//
//      if(p.shape.position.y < bb.minY || p.shape.position.y > bb.maxY){
//        p.v.y = math.signum(bb.centerY - p.shape.position.y)*math.abs(p.v.y)
//      }
//
//      p.v += Float2.randomMinusOneToOne*0.03f
//
//      val leader = ps(0)
//      if(p.ix != leader.ix){
//        val diff = (leader.shape.position - p.shape.position).unit * 0.1f
//        p.v += diff
//      } else {
//        p.v += Float2.randomMinusOneToOne*0.2f
//      }
//
//      p.v *= 0.9995f
//
//
//    }
//    //if(Math.random() < 0.9) return
//
//
//
//
//  }
//
//  var myPolygon1: Polygon = Polygon(pentagon, scale = 200f)
//
//  var myPolygon2: Polygon = myPolygon1.scale(0.5f)
//
//  def drawPoints[E <: Float2]()(g2d: Graphics2D): Unit ={
//    // draw glow
//    val glowColor = new Color(1f, 1f, 1f/*, 0.1f*/)
//    ps.foreach { e =>
//      if (collisionCollector.colliding(e.ix)) {
//        drawCircle(e, radius = 5f, color = glowColor)(g2d)
//      }
//    }
//
//    ps.foreach { e =>
//      val color = if (collisionCollector.colliding(e.ix)) Color.YELLOW else Color.MAGENTA
//      drawEdgeCircle(e, radius = 10f, faceColor = color)(g2d)
//      val polyColor = if (collisionCollector.colliding(e.ix)) Color.RED else Color.GREEN
//      drawPolygon(e.shape.asInstanceOf[Polygon], polyColor)(g2d)
//    }
//
//    drawPolygon(myPolygon1, edgeColor = Color.CYAN)(g2d)
//    drawPolygon(myPolygon2, edgeColor = Color.CYAN)(g2d)
//    // drawEdgeCircle(ps(0), radius = 20f, faceColor = Color.RED)(g2d)
//  }
//
//  def drawTree[E <: Float2](tree: QuadTree[E])(g2d: Graphics2D): Unit ={
//    val nodes: Seq[AABB] = tree.root.nodes.map{_.bounds}
//    nodes.foreach(b => drawAABB(b, Color.DARK_GRAY)(g2d))
//  }
//
//  val treeParams = Parameters(nodeElementCapacity = 48)
//
//  val bbWithMargin: AABB = AABB.addMargin(bb, 100)
//
//  var tree: QuadTree[DataPoint[Body]] = QuadTree[DataPoint[Body]](bbWithMargin, treeParams)
//
//  tree.add(ps.map{p => DataPoint[Body](p.shape.position, p)})
//
//  val panel = new FlippedDrawingPanel(bb.width.toInt, bb.height.toInt, Color.BLACK,
//    BoundedDrawingFunction(drawTree(tree)(_), () => bb),
//    //BoundedDrawingFunction(drawGlow()(_), () => bb),
//    BoundedDrawingFunction(drawPoints()(_), () => bb)
//  )
//  if (visualize) new Frame("Bouncing", panel)
//
//
//  val msPerFrame = 16
//  var ts: Double = System.nanoTime()/1e6
//
//
//  val fpss = ArrayBuffer[Double](240)
//
//  def measureFps(f: Array[VelPoint] => Unit, name: String): Double ={
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
//  //Thread.sleep(10000)
//  def measureMove(): Double = measureFps(
//    (ps: Array[VelPoint]) => ps.foreach(_.velMove()), "MOVE"
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
//
//  //measureMove()
//  //measureMoveAndInsert()
//  //measureAllCollisionInnerLeaf()
//  //measureAllCollision()
//
//  for(t <- 0 until timeSteps){
//    ps.foreach(_.velMove())
//    tree = QuadTree[VelPoint](bbWithMargin, treeParams)
//    tree.add(ps)
//    updateCollisionStatus(tree, ps)
//
//    myPolygon1 = myPolygon1.moveTo(ps(0)).rotate(0.005f)
//    myPolygon2 = myPolygon2.moveTo(ps(1)).rotate(-0.01f)
//
//
//
//    val dt = System.nanoTime()/1e6 - ts
//
//    if(visualize){
//      val sleepTime = msPerFrame - dt
//      panel.validate()
//      panel.repaint()
//      if(sleepTime > 0) Thread.sleep(sleepTime.toInt)
//    }
//
//
//    //Thread.sleep(200)
//    fpss += 1000.0/dt
//    if(t != 0 && t % 240 == 0) {
//      val fps = fpss.sum/fpss.size
//      println(s"FPS: ${fps.toInt} (${1000/fps})")
//      fpss.clear();
//    }
//
//    ts = System.nanoTime()/1e6
//  }
//}
