package com.scilari.geometry.renderer

import org.scalajs.dom.html.Canvas
import org.scalajs.dom.CanvasRenderingContext2D
import com.scilari.geometry.models._
import com.scilari.math.FloatMath._
import org.scalajs.dom.raw.HTMLImageElement
import com.scilari.geometry.models.LineSegment


case class DrawingFunctions(canvas: Canvas) {
  val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  def image(image: HTMLImageElement, center: Float2, rotation: Float = 0f, pxToMeter: Double): Unit = {
    ctx.save()
    ctx.translate(center.x, center.y)
    ctx.rotate(rotation)
    ctx.scale(pxToMeter, pxToMeter)
    ctx.save()
    ctx.transform(1, 0, 0, -1, -image.width / 2, image.height / 2)
    ctx.drawImage(image, 0, 0)
    ctx.restore()

    ctx.restore()
  }

  def lineSegment(segment: LineSegment, color: String = "red"): Unit = {
    ctx.strokeStyle = color
    ctx.beginPath()
    ctx.moveTo(segment.p1.x, segment.p1.y)
    ctx.lineTo(segment.p2.x, segment.p2.y)
    ctx.stroke()
  }

  def rect(box: AABB, color: String = "grey"): Unit = {
    ctx.strokeStyle = color
    ctx.strokeRect(box.bottomLeft.x, box.bottomLeft.y, box.width, box.height)
  }

  def filledRect(box: AABB, fillColor: String = "grey") = {
    ctx.fillStyle = fillColor
    ctx.fillRect(box.bottomLeft.x, box.bottomLeft.y, box.width, box.height)
  }

  def circle(c: Circle, color: String): Unit = circle(c.center, c.r, color)

  def circle(center: Float2, r: Float, color: String = "grey"): Unit = {
    ctx.beginPath()
    ctx.strokeStyle = color
    ctx.arc(center.x, center.y, r, 0, 2 * Math.PI)
    ctx.stroke()
  }


  def filledCircle(c: Circle, color: String): Unit = {
    filledCircle(c.center, c.r, color)
  }

  // TODO: fix
  def filledCircle(center: Float2, r: Float, fillColor: String = "grey", alpha: Double = 1.0): Unit = {
    ctx.save()
    ctx.globalAlpha = alpha
    ctx.beginPath()
    ctx.fillStyle = fillColor
    ctx.arc(center.x, center.y, r, 0, 2 * Math.PI)
    ctx.fill()
    ctx.restore()
  }

  def pose(pose: Pose, r: Float, fillColor: String = "green"): Unit = {
    val poseAlpha = 0.15

    val triangle: Array[Float2] = {
      val x = pose.position.x
      val y = pose.position.y
      val a = pose.heading.value
      Array(
        Float2(x + 0.9 * r * cos(a), y + 0.9 * r * sin(a)),
        Float2(x + 0.2 * r * cos(a + HalfPi), y + 0.2 * r * sin(a + HalfPi)),
        Float2(x + 0.2 * r * cos(a - HalfPi), y + 0.2 * r * sin(a - HalfPi))
      )
    }

    val outLineColor = "BLACK"
    circle(pose.position, r, outLineColor)
    filledCircle(pose.position, r, fillColor, poseAlpha)

    ctx.strokeStyle = outLineColor
    ctx.fillStyle = fillColor
    ctx.beginPath()
    ctx.moveTo(triangle(2).x, triangle(2).y)
    for (p <- triangle) {
      ctx.lineTo(p.x, p.y)
    }
    ctx.stroke()

    ctx.save()
    ctx.globalAlpha = poseAlpha
    ctx.fill()
    ctx.restore()

    //ctx.strokeStyle = color

  }

}
