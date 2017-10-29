package com.scilari.geometry.spatialsearch.plotting

import java.awt.{Color, Graphics2D}

import com.scilari.geometry.models.{AABB, Float2}
import com.scilari.geometry.spatialsearch.SearchTree.{BoxBounded, Concrete}
import com.scilari.geometry.spatialsearch.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}
import com.scilari.geometry.spatialsearch.rtree.RTree

object TreePlotter {
  def plot[E <: Float2](tree: Concrete[E], frameName: String = "Tree", width: Int = 1000, height: Int = 1000): Unit ={
    val panel = new FlippedDrawingPanel(width, height, Color.WHITE, (drawTree(tree) _, tree.root.asInstanceOf[AABB]))
    val frame = new Frame(frameName, panel)
  }

  def drawTree[E <: Float2](tree: Concrete[E])(g2d: Graphics2D): Unit ={
    val nodes: Seq[AABB] = tree.root.nodes.map{_.asInstanceOf[AABB]}
    val elems: Seq[Float2] = tree.root.elements

    nodes.foreach(b => drawAABB(b, Color.BLACK)(g2d))
    elems.foreach(e => drawEdgeCircle(e, radius = 0.005f)(g2d))

  }

  def main(args: Array[String]): Unit ={
    val k = 200
    val points = Seq.fill(k)(Float2.random()) ++ Seq.fill(k)(Float2.random + 1.1f)
    val tree = QuadTree[Float2](points, parameters = Parameters(nodeElementCapacity = 8))
    TreePlotter.plot(tree)

    val rtree = RTree[Float2](points)
    TreePlotter.plot(rtree)

    println("Lol")
  }

}
