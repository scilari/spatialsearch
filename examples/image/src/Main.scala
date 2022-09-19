package com.scilari.geometry.renderer

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.document
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

import com.scilari.geometry.models.{Float2, AABB}
import com.scilari.geometry.models.Circle
import com.scilari.math.FloatMath._

import com.scilari.geometry.spatialsearch.quadtree.QuadTree

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello examples/image!")

    def points = Seq.fill(1000)(Float2.random(10)) ++ Seq.fill(1000)(Float2.random(10) + 5)
    val tree = QuadTree[Float2](points)

    val canvas: Canvas = document.getElementById("canvas").asInstanceOf[Canvas]
    val imageBounds = AABB.positiveSquare(15)
    val visualBounds = imageBounds.withMargin(1f)

    val image = document.createElement("img").asInstanceOf[HTMLImageElement]
    image.src = "../resources/koostra_maze.png"

    val df = DrawingFunctions(canvas)
    var theta = 0f

    val renderFunction = () => {
      df.image(image, imageBounds.center + 5, theta, imageBounds.width / image.width)
      df.filledCircle(Circle(Float2(2, 2), 5 + sin(10 * theta)), "red")
      df.rect(visualBounds.withMargin(cos(10 * theta)), "green")
      tree.root.leaves.foreach { l => df.rect(l.bounds, "black") }
    }

    val boundFunction = () => visualBounds.withMargin(cos(10 * theta))

    val renderer = BoundedRenderer(
      canvas,
      renderFunction,
      boundFunction
    )

    image.onload = (e: dom.Event) => {
      println("Image loaded")
      start()
    }

    def start(): Unit = {

      dom.window.setInterval(
        () => {
          theta += 0.01f
          renderer.render()
        },
        100
      )
    }

  }
}
