package com.scilari.geometry

import java.awt._
import java.awt.geom._
import java.awt.image.BufferedImage

import com.scilari.geometry.models.{AABB, Float2}


/**
  * Created by iv on 26.2.2014.
  */
package object plotting {
  implicit def graphicsToGraphics2D(g: Graphics): Graphics2D = g.asInstanceOf[Graphics2D]
  implicit def AABBtoRect(b: AABB): Rectangle2D = new Rectangle2D.Float(b.minX, b.minY, b.width, b.height)

  class BoundedDrawingFunction(val drawingFunction: Graphics2D => Any, val bounds: () => AABB)

  implicit def boxPairToBoundedDrawingFunction(fb: (Graphics2D => Any, AABB)): BoundedDrawingFunction =
    fb match{ case(f: (Graphics2D => Any), b: AABB) => new BoundedDrawingFunction(f, () => b)}

  implicit def pairToBoundedDrawingFunction(fb: (Graphics2D => Any, () => AABB)): BoundedDrawingFunction =
    fb match{ case(f: (Graphics2D => Any), b: (() => AABB)) => new BoundedDrawingFunction(f, b)}

  implicit def functionToBoundedDrawingFunction(f: Graphics2D => Any): BoundedDrawingFunction =
    new BoundedDrawingFunction(f, () => AABB.zero)


  def drawAABB(box: AABB, color: Color = Color.GRAY)(g2d: Graphics2D){
    g2d.setPaint(color)
    g2d.draw(box)
  }

  def drawPoints[T <: Float2](points: Traversable[T], color: Color = Color.RED, radius: Float)(g2d: Graphics2D){
    points.foreach{drawPoint(_, color, radius)(g2d)}
  }


  def drawPoint[T <: Float2](point: T, color: Color = Color.RED, radius: Float)(g2d: Graphics2D){
    g2d.setPaint(color)
    val rect = new Rectangle2D.Float(point.x - radius, point.y - radius, 2*radius, 2*radius)
    g2d.draw(rect)
    //g2d.drawRect(point.x.toInt-radius, point.y.toInt-radius, 2*radius, 2*radius)
  }

  def drawFilledCircle[T <: Float2](point: T, color: Color = Color.RED, radius: Float)(g2d: Graphics2D): Unit ={
    val twoR = 2*radius
    val circle = new java.awt.geom.Ellipse2D.Double(point.x - radius, point.y - radius, twoR, twoR)
    g2d.setColor(color)
    g2d.fill(circle)
  }

  def drawCircle[T <: Float2](point: T, color: Color = Color.RED, radius: Float)(g2d: Graphics2D): Unit ={
    val twoR = 2*radius
    val circle = new java.awt.geom.Ellipse2D.Double(point.x - radius, point.y - radius, twoR, twoR)
    g2d.setColor(color)
    g2d.draw(circle)
  }

  def drawEdgeCircle[T <: Float2](point: T, faceColor: Color = Color.RED, edgeColor: Color = Color.BLACK, radius: Float)(g2d: Graphics2D): Unit ={
    // TODO: edge width
    val twoR = 2*radius
    val circle = new java.awt.geom.Ellipse2D.Double(point.x - radius, point.y - radius, twoR, twoR)
    g2d.setColor(faceColor)
    g2d.fill(circle)
    g2d.setColor(edgeColor)
    g2d.draw(circle)

  }

  def drawFilledPoints[T <: Float2](points: Traversable[T], color: Color = Color.RED, radius: Float)(g2d: Graphics2D){
    points.foreach{drawFilledPoint(_, color, radius)(g2d)}
  }

  def drawFilledPoint[T <: Float2](point: T, color: Color = Color.RED, radius: Float)(g2d: Graphics2D){
    g2d.setPaint(color)
    val rect = new Rectangle2D.Float(point.x - radius, point.y - radius, 2*radius, 2*radius)
    g2d.fill(rect)
  }

  def drawScaledBitmap(image: BufferedImage, imgPxPerMeter: Float)(g2d: Graphics2D): Unit = {
    g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight,
      0, 0, (imgPxPerMeter*image.getWidth).toInt, (imgPxPerMeter*image.getHeight).toInt, null)
  }


  def colorWithAlpha(original: Color, alpha: Float): Color = {
    new Color(original.getRed, original.getGreen, original.getBlue, (alpha*255f).toInt)
  }

  def colorFromArray(a: Array[Float]): Color ={
    new Color(a(0), a(1), a(2), a(3))
  }


  def flippedImage(image: BufferedImage): BufferedImage = {
    val at = new AffineTransform()
    at.concatenate(AffineTransform.getScaleInstance(1, -1))
    at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()))
    createTransformedImage(image, at)
  }

  def createTransformedImage(image: BufferedImage, at: AffineTransform): BufferedImage = {
    val newImage = new BufferedImage(
      image.getWidth(), image.getHeight(),
      BufferedImage.TYPE_INT_ARGB)
    val g = newImage.createGraphics()
    g.transform(at)
    g.drawImage(image, 0, 0, null)
    g.dispose()
    newImage
  }


}
