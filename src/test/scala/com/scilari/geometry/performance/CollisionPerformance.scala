package com.scilari.geometry.performance

import java.awt.{Color, Graphics2D}
import java.util.concurrent.ArrayBlockingQueue

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.plotting.{BoundedDrawingFunction, drawAABB, drawEdgeCircle}
import com.scilari.geometry.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.spatialsearch.trees.quadtree.{Parameters, QuadTree}
import org.scalatest.{FlatSpec, Matchers}

import scala.annotation.tailrec
import scala.collection.mutable
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
  val n = 10000
  val timeSteps = 60*30
  val visualize = true



  class VelPoint(xx: Float, yy: Float, var ix: Int, var v: Float2, val r: Float = 5f) extends Float2(xx, yy) {
    var isColliding: Boolean = false
    var isHandled: Boolean = true

    def velMove(): Unit = {
      // if(Math.random() < 0.9) return

      this += v

      if(x < bb.minX || x > bb.maxX){
        v.x = math.signum(bb.centerX - x)*math.abs(v.x)
      }

      if(y < bb.minY || y > bb.maxY){
        v.y = math.signum(bb.centerY - y)*math.abs(v.y)
      }

      v += Float2.randomMinusOneToOne*0.03f

      val leader = ps(0)
      if(!this.equalCoordinates(leader)){
        val diff = (leader - this).unit * 0.1f
        v += diff
      } else {
        v += Float2.randomMinusOneToOne*0.1f
      }

      v = v*0.9995f
      //val maxV = 10f
      //v = v.clamp(-Float2.one*maxV, Float2.one*maxV)

    }
  }

  def createPs: Array[VelPoint] = {
    val radii = Array.fill(n)((2.0 + Math.random () * 10.0)/5).sorted.reverse
    radii.zipWithIndex.map{ case(r, ix) =>
      val rp = bb.randomEnclosedPoint
      val vBox = AABB.fromMinMax (- 2, - 2, 2, 2)
      new VelPoint (rp.x, rp.y, ix, vBox.randomEnclosedPoint, r.toFloat)
    }
  }

  val ps = createPs


  def drawPoints[E <: Float2]()(g2d: Graphics2D): Unit ={
    ps.foreach{ e =>
      val color = if(e.isColliding) Color.GRAY else Color.MAGENTA
      drawEdgeCircle(e, radius = e.r, faceColor = color)(g2d)
    }
    drawEdgeCircle(ps(0), radius = 20f, faceColor = Color.RED)(g2d)
  }

  def drawTree[E <: Float2](tree: QuadTree[E])(g2d: Graphics2D): Unit ={
    val nodes: Seq[AABB] = tree.root.nodes.map{_.bounds}
    nodes.foreach(b => drawAABB(b, Color.BLACK)(g2d))
  }

  val treeParams = Parameters(nodeElementCapacity = 48)

  val bbWithMargin: AABB = AABB.addMargin(bb, 100)

  var tree: QuadTree[VelPoint] = QuadTree[VelPoint](bbWithMargin, treeParams)

  tree.add(ps)

  val panel = new FlippedDrawingPanel(bb.width.toInt, bb.height.toInt, Color.WHITE,
    BoundedDrawingFunction(drawTree(tree)(_), () => bb),
    BoundedDrawingFunction(drawPoints()(_), () => bb)
  )
  if (visualize) new Frame("Bouncing", panel)


  val msPerFrame = 16
  var ts: Double = System.nanoTime()/1e6


  def collides(a: VelPoint, b: VelPoint): Boolean = {
    a.ix < b.ix && {
      val d = a.r + b.r
      a.distanceSq(b) < d*d
    }
  }

  class Collisions[T](){
    def add(a: T, b :T): Unit = { as += a; bs += b }
    def clear(): Unit ={
      as.clear(); bs.clear();
    }
    val as = new ArrayBuffer[T]()
    val bs = new ArrayBuffer[T]()
  }

  val collisionPairs = new Collisions[VelPoint]()

  //val initialLeaves = tree.root.leaves

  def updateCollisionLeaves(tree: QuadTree[VelPoint], ps: Seq[VelPoint]): Unit ={
    //val leaves = initialLeaves
    val leaves = tree.root.leaves
    leaves.foreach{ leaf =>
      val es = leaf.elements
      val n = es.length
      var i = 0
      while(i < n){
        val a = es(i)
        val aBox = AABB(a, 2*a.r)
        a.isHandled = leaf.bounds.contains(aBox)

        if(a.isHandled){
          addToCollisions(a, es, i + 1)
        }
        i += 1
      }
    }
  }


  def updateCollisionsNonHandled(tree: QuadTree[VelPoint], ps: Seq[VelPoint]): Unit ={
    ps.foreach{ p =>
      if(!p.isHandled){
        val nbs = tree.rangeSearch(p, 2*p.r)
        addToCollisions(p, nbs)
      }
    }
  }

  def addToCollisions(a: VelPoint, candidates: Seq[VelPoint], startingIndex: Int = 0): Unit ={
    var i = startingIndex
    val n = candidates.size
    while(i < n){
      val b = candidates(i)
      if(collides(a, b)){
        collisionPairs.add(a, b)
      }
      i += 1
    }
  }

  def updateCollisionStatus(tree: QuadTree[VelPoint], ps: Seq[VelPoint], inLeafOnly: Boolean = false): Unit ={
    ps.foreach{p =>
      p.isColliding = false
      p.isHandled = false
    }

    updateCollisionLeaves(tree, ps)
    if(!inLeafOnly) updateCollisionsNonHandled(tree, ps)

    collisionPairs.as.indices.foreach{ ix =>
      collisionPairs.as(ix).isColliding = true
      collisionPairs.bs(ix).isColliding = true
    }

    collisionPairs.clear()
  }

  val fpss = ArrayBuffer[Double](60)

  def measureFps(f: Array[VelPoint] => Unit, name: String): Double ={
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
  def measureMove(): Double = measureFps(
    (ps: Array[VelPoint]) => ps.foreach(_.velMove()), "MOVE"
  )


  def measureMoveAndInsert(): Double = measureFps(
    (ps: Array[VelPoint]) => {
      tree = QuadTree[VelPoint](bbWithMargin, treeParams)
      tree.add(ps)
      ps.foreach(_.velMove())
    }, "MOVE AND INSERT")

  def measureAllCollisionInnerLeaf(): Double = measureFps(
    (ps: Array[VelPoint]) => {
      tree = QuadTree[VelPoint](bbWithMargin, treeParams)
      tree.add(ps)
      ps.foreach(_.velMove())
      updateCollisionStatus(tree, ps, true)
    }, "MOVE AND INSERT AND INLEAF")

  def measureAllCollision(): Double = measureFps(
    (ps: Array[VelPoint]) => {
      tree = QuadTree[VelPoint](bbWithMargin, treeParams)
      tree.add(ps)
      ps.foreach(_.velMove())
      updateCollisionStatus(tree, ps)
    }, "MOVE AND INSERT AND COLLISIONS ALL")

  measureMove()
  measureMoveAndInsert()
  measureAllCollisionInnerLeaf()
  measureAllCollision()

  for(t <- 0 until timeSteps){
    tree = QuadTree[VelPoint](bbWithMargin, treeParams)
    tree.add(ps)

    updateCollisionStatus(tree, ps)

    ps.foreach(_.velMove())

    val dt = System.nanoTime()/1e6 - ts

    if(visualize){
      val sleepTime = msPerFrame - dt
      if(sleepTime > 0) Thread.sleep(sleepTime.toInt)
      panel.validate()
      panel.repaint()
    }


    //Thread.sleep(500)
    fpss += 1000.0/dt
    if(t != 0 && t % 240 == 0) {
      val tempFps = fpss.takeRight(240)
      val fps = tempFps.sum/tempFps.size
      println(s"FPS: ${fps.toInt} (${1000/fps})")
    }

    ts = System.nanoTime()/1e6
  }



  val fps = fpss.drop(60).sum/(fpss.size - 60)
  println(s"FINAL FPS: ${fps.toInt} (${1000/fps})")



}
