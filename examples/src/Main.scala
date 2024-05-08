package com.scilari.geometry

import com.scilari.geometry.models.Float2

import doodle.java2d.*
import cats.effect.unsafe.implicits.global
import doodle.syntax.all.*
import doodle.core.*
import doodle.java2d.effect.*
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

object Main {
  def main(args: Array[String]): Unit = {
    val canvas = Frame.default
      .withSize(1000, 1000)
      .withCenterAtOrigin
      .canvas()
      .unsafeRunSync()

    def points = Seq.fill(100)(Float2.random(200) - 500) ++ Seq.fill(100)(Float2.random(1000) - 500)
    val tree = QuadTree[Float2](points)

    val bounds = tree.root.bounds
    var picture = rectangle(width = bounds.width, height = bounds.height).at(bounds.x, bounds.y)

    tree.root.leaves.foreach { leaf =>
      val b = leaf.bounds
      picture = picture.on(rectangle(b.width, b.height).at(b.x, b.y))
    }

    points.foreach { p =>
      picture = picture.on(circle(10).fillColor(Color.red).at(p.x, p.y).asInstanceOf)
    }

    picture.drawWithCanvas(canvas)

  }

}
