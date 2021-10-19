package com.scilari.geometry.spatialsearch.plotting

import java.awt.{Color, Graphics2D}

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.plotting.Plotting._
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}

object TreePlotter {
  def plot[E <: Float2](
    tree: QuadTree[E],
    frameName: String = "Tree",
    width: Int = 800,
    height: Int = 800,
    elemRadius: Float = 5f
  ): Unit ={
    val bb = AABB.enclosingSquare(tree.root.bounds.corners, margin = 0.02f*tree.root.bounds.width)
    val panel = new FlippedDrawingPanel(width, height, Color.WHITE,
      BoundedDrawingFunction(drawTree(tree, elemRadius)(_), bb))
    new Frame(frameName, None, panel)
  }

  def drawTree[E <: Float2](tree: QuadTree[E], elemRadius: Float)(g2d: Graphics2D): Unit ={
    val nodes: collection.Seq[AABB] = tree.root.nodes.map{_.bounds}
    val elems: collection.Seq[Float2] = tree.root.elements

    //g2d.scale(1f, 0.5f)
    //g2d.rotate(Math.PI/4)

    nodes.foreach(b => drawAABB(b, Color.BLACK)(g2d))
    elems.foreach(e => drawEdgeCircle(e, radius = elemRadius, faceColor = Color.MAGENTA)(g2d))
  }

  def main(args: Array[String]): Unit ={
    val k = 200
    val points = Seq.fill(k)(Float2.random(1000f)) ++ Seq.fill(k)(Float2.random(1000f) + 1100f)
    val quad: QuadTree[Float2] = QuadTree[Float2](points, parameters = Parameters(nodeElementCapacity = 8))
    TreePlotter.plot(quad, "QuadTree")

    //val rtree: RTree[Float2] = RTree[Float2](points, nodeElementCapacity = 8)
    //TreePlotter.plot(rtree, "RTree")
  }

}
