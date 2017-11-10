package com.scilari.geometry.spatialsearch.plotting

import java.awt.{Color, Graphics2D}

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.SearchTree
import com.scilari.geometry.spatialsearch.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}
import com.scilari.geometry.spatialsearch.rtree.RTree

object TreePlotter {
  def plot[E <: Float2](
    tree: SearchTree[E],
    frameName: String = "Tree",
    width: Int = 800,
    height: Int = 800,
    elemRadius: Float = 5f
  ): Unit ={
    val bb = AABB(tree.root.corners, margin = 0.05f*tree.root.width)
    val panel = new FlippedDrawingPanel(width, height, Color.WHITE, (drawTree(tree, elemRadius) _, bb))
    new Frame(frameName, panel)
  }

  def drawTree[E <: Float2](tree: SearchTree[E], elemRadius: Float)(g2d: Graphics2D): Unit ={
    val nodes: Seq[AABB] = tree.root.nodes.map{_.asInstanceOf[AABB]}
    val elems: Seq[Float2] = tree.root.elements

    nodes.foreach(b => drawAABB(b, Color.BLACK)(g2d))
    elems.foreach(e => drawEdgeCircle(e, radius = elemRadius)(g2d))

  }

  def main(args: Array[String]): Unit ={
    val k = 200
    val points = Seq.fill(k)(Float2.random(1000f)) ++ Seq.fill(k)(Float2.random(1000f) + 1100f)
    val quad = QuadTree[Float2](points, parameters = Parameters(nodeElementCapacity = 8))
    TreePlotter.plot(quad, "QuadTree")

    val rtree = RTree[Float2](points, nodeElementCapacity = 8)
    TreePlotter.plot(rtree, "RTree")
  }

}
