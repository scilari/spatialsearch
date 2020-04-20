package com.scilari.geometry.plotting

import java.awt.{Color, Graphics2D}

import com.scilari.geometry.collisions.Scene
import com.scilari.geometry.models.shapes.{Circle, Polygon, Segment}
import com.scilari.geometry.models.{AABB, Body, Float2, HasPosition}
import com.scilari.geometry.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.spatialsearch.TestUtils
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree
import com.scilari.math.sqrt

class SceneRenderer(val scene: Scene, val bounds: AABB) {

  lazy val boxWood = TestUtils.loadImage("src/test/resources/boxAlt.png")
  lazy val boxGrass = TestUtils.loadImage("src/test/resources/grassMid.png")

  var debugString: Seq[String] = Seq("Hello", "World")


  def update(): Unit ={
    panel.validate()
    panel.repaint()
  }


  def drawPoints[E <: Float2]()(g2d: Graphics2D): Unit ={
    scene.bodies.foreach { b =>
      b.shape match {
        case p: Polygon => drawPolygonalShape(p,b)(g2d)
        case c: Circle => drawCircularShape(c)(g2d)
        case s: Segment => drawSegmentShape(s)(g2d)
        case _ => ()
      }
    }
    //drawContacts(g2d)
    drawDebugString(g2d)
  }

  def drawDebugString(g2d: Graphics2D): Unit ={
    val base = Float2(10, 20)
    val offset = Float2(0, 20)
    debugString.zipWithIndex.foreach{ case (s, i) =>
      drawString(s, base + offset * i, bounds.height)(g2d)
    }
  }

  def drawSegmentShape(segment: Segment)(g2d: Graphics2D): Unit ={
    val s = segment.lineSegment
    drawLine(s.p1, s.p2, color = Color.MAGENTA)(g2d)
    //drawPoint(b.position, color = Color.RED, radius = 2)(g2d)
  }

  def drawCircularShape(c: Circle)(g2d: Graphics2D): Unit = {
    drawCircle(c.position, Color.CYAN, radius = c.radius)(g2d)
    val directedRadius = Float2.directed(c.transform.rotation, c.radius)
    drawLine(c.position, c.position + directedRadius, color = Color.CYAN)(g2d)
  }

  def drawPolygonalShape(p: Polygon, b: Body)(g2d: Graphics2D): Unit ={
    if(p.points.length == 4) {
      drawBitmap(
        p,
        if(b.static) boxGrass else boxWood,
        scale = 1f/sqrt(2f))(g2d)
    } else {
      val polyColor = if(b.static) Color.BLUE else Color.CYAN
      drawPolygon(p, polyColor)(g2d)
    }

  }

  def drawContacts(g2d: Graphics2D): Unit ={
    val radius = 2
    scene.collisionCollector.collisions.clone().foreach { c =>
      if(c != null) {
        c.contactPoints.foreach { p =>
          drawPoint(p, Color.WHITE, radius = radius)(g2d)
        }
        drawPoint(c.meanContact, Color.MAGENTA, radius = radius)(g2d)
      }
    }

  }

  def drawTree[E <: HasPosition](tree: QuadTree[E])(g2d: Graphics2D): Unit ={
    val nodes: Seq[AABB] = tree.root.nodes.map{_.bounds}
    nodes.foreach(b => drawAABB(b, Color.DARK_GRAY)(g2d))
  }


  val panel = new FlippedDrawingPanel(bounds.width.toInt, bounds.height.toInt, Color.BLACK,
    BoundedDrawingFunction(drawTree(scene.tree)(_), () => bounds),
    BoundedDrawingFunction(drawPoints()(_), () => bounds)
  )

  val frame = new Frame("Bouncing", panel)

}
