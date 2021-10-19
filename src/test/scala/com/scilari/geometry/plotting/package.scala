package com.scilari.geometry.plotting

import java.awt._
import java.awt.geom._
import java.awt.image.BufferedImage

import scala.language.implicitConversions

import com.scilari.geometry.models.{AABB, Float2/*, shapes, Shape => ScilariShape*/}

object Plotting {
  implicit def graphicsToGraphics2D(g: Graphics): Graphics2D = g.asInstanceOf[Graphics2D]
  implicit def AABBtoRect(b: AABB): Rectangle2D = new Rectangle2D.Float(b.minX, b.minY, b.width, b.height)
  implicit def AABBtoFunc(b: AABB): () => AABB = () => b

  case class BoundedDrawingFunction(drawingFunction: Graphics2D => Any, bounds: () => AABB)

  implicit def boxPairToBoundedDrawingFunction(fb: (Graphics2D => Any, AABB)): BoundedDrawingFunction =
    fb match{ case(f: (Graphics2D => Any), b: AABB) => BoundedDrawingFunction(f, () => b)}

  implicit def pairToBoundedDrawingFunction(fb: (Graphics2D => Any, () => AABB)): BoundedDrawingFunction =
    fb match{ case(f: (Graphics2D => Any), b: (() => AABB)) => BoundedDrawingFunction(f, b)}

  implicit def functionToBoundedDrawingFunction(f: Graphics2D => Any): BoundedDrawingFunction =
    BoundedDrawingFunction(f, () => AABB.zero)


  def drawString(str: String, screenPosition: Float2, height: Float)(g2d: Graphics2D): Unit ={
    val tr = g2d.getTransform
    val scale = 0.1f
    g2d.scale(scale,-scale)
    g2d.translate(0,-height)
    g2d.setColor(Color.WHITE)
    g2d.drawString(str, screenPosition.x, screenPosition.y)
    g2d.setTransform(tr)
  }


  def drawAABB(box: AABB, color: Color = Color.GRAY)
    (implicit g2d: Graphics2D): Unit = {
    g2d.setPaint(color)
    g2d.draw(box)
  }

//  def drawPolygon(polygon: shapes.Polygon, edgeColor: Color = Color.RED)(g2d: Graphics2D): Unit = {
//    val xs = polygon.points.map(_.x.toInt)
//    val ys = polygon.points.map(_.y.toInt)
//    g2d.setColor(edgeColor)
//    //g2d.setStroke(new BasicStroke(2))
//    //g2d.fillPolygon(xs, ys, xs.length)
//    g2d.drawPolygon(xs, ys, xs.length)
//
//  }

  def drawLine(p1: Float2, p2: Float2, color: Color)(g2d: Graphics2D): Unit = {
    g2d.setColor(color)
    g2d.draw(new Line2D.Float(p1.x, p1.y, p2.x, p2.y))
  }

  def drawPoints[T <: Float2](points: Traversable[T], color: Color = Color.RED, radius: Float)(
    implicit g2d: Graphics2D): Unit = {
    points.foreach{drawPoint(_, color, radius)(g2d)}
  }


  def drawPoint[T <: Float2](point: T, color: Color = Color.RED, radius: Float)(
    implicit g2d: Graphics2D): Unit = {
    g2d.setPaint(color)
    val rect = new Rectangle2D.Float(point.x - radius, point.y - radius, 2*radius, 2*radius)
    g2d.draw(rect)
  }

  def drawFilledCircle[T <: Float2](point: T, color: Color = Color.RED, radius: Float)
    (implicit g2d: Graphics2D): Unit ={
    val twoR = 2*radius
    val circle = new java.awt.geom.Ellipse2D.Double(point.x - radius, point.y - radius, twoR, twoR)
    g2d.setColor(color)
    g2d.fill(circle)
  }

  def drawCircle[T <: Float2](point: T, color: Color = Color.RED, radius: Float)
    (implicit g2d: Graphics2D): Unit ={
    val twoR = 2*radius
    val circle = new java.awt.geom.Ellipse2D.Double(point.x - radius, point.y - radius, twoR, twoR)
    g2d.setStroke(new BasicStroke(2))
    g2d.setColor(color)
    g2d.draw(circle)
  }

  def drawEdgeCircle[T <: Float2](point: T, faceColor: Color = Color.RED, edgeColor: Color = Color.BLACK, radius: Float)
    (implicit g2d: Graphics2D): Unit ={
    // TODO: edge width
    val twoR = 2*radius
    val circle = new java.awt.geom.Ellipse2D.Double(point.x - radius, point.y - radius, twoR, twoR)
    g2d.setColor(faceColor)
    g2d.fill(circle)
    g2d.setColor(edgeColor)
    g2d.draw(circle)

  }

  def drawFilledPoints[T <: Float2](points: Traversable[T], color: Color = Color.RED, radius: Float)
    (implicit g2d: Graphics2D): Unit ={
    points.foreach{drawFilledPoint(_, color, radius)(g2d)}
  }

  def drawFilledPoint[T <: Float2](point: T, color: Color = Color.RED, radius: Float)
    (implicit g2d: Graphics2D): Unit ={
    g2d.setPaint(color)
    val rect = new Rectangle2D.Float(point.x - radius, point.y - radius, 2*radius, 2*radius)
    g2d.fill(rect)
  }

//  def drawBitmap(shape: ScilariShape, image: BufferedImage, scaleX: Float, scaleY: Float = -1.0f)(g2d: Graphics2D): Unit = {
//    val scX = scaleX/image.getWidth()
//    val scY = if(scaleY < 0f) scaleX/image.getHeight() else scaleY/image.getHeight()
//
//    val offset = shape.position
//    val centerImage = Float2(-image.getWidth()/2, -image.getHeight()/2)
//
//    val tr: AffineTransform = new AffineTransform()
//    tr.scale(scX, scY)
//    tr.translate(offset.x/scX + centerImage.x, offset.y/scY + centerImage.y)
//    tr.rotate(shape.transform.rotation + com.scilari.math.Pi, image.getWidth / 2, image.getHeight / 2)
//
//    g2d.drawImage(image, tr, null)
//  }

  def drawScaledBitmap(image: BufferedImage, imgPxPerMeter: Float)
    (implicit g2d: Graphics2D): Unit = {
    val x = (imgPxPerMeter*image.getWidth).toInt
    val y = (imgPxPerMeter*image.getHeight).toInt
    g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight,
      0, 0, x , y, null)
  }


  def colorWithAlpha(original: Color, alpha: Float): Color = {
    new Color(original.getRed, original.getGreen, original.getBlue, (alpha*255f).toInt)
  }

  def colorFromArray(a: Array[Float]): Color ={
    new Color(a(0), a(1), a(2), a(3))
  }



}
