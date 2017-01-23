package com.scilari.geometry.spatialsearch.plotting

import java.awt._
import java.io.File
import javax.imageio.ImageIO
import javax.swing.{JFrame, JPanel}

import com.scilari.geometry.models.AABB
import com.scilari.math._

/**
 * Created by iv on 8/20/2015.
 */
object Panels {

  abstract class Panel(val w: Int, val h: Int, backgroundColor: Color = Color.BLACK) extends JPanel {
    val serialVersionUID = 1L
    setSize(w, h)
    setPreferredSize(this.getSize())
    setBackground(backgroundColor)
    var g2d: Graphics2D = null

    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      g2d = g.asInstanceOf[Graphics2D]
    }
  }

  class BitmapPanel(filename: String, w: Int, h: Int, backgroundColor: Color) extends Panel(w, h, backgroundColor) {
    val image = ImageIO.read(new File(filename))

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)
      g2d.drawImage(image, 0, 0, w, h, 0, 0, image.getWidth, image.getHeight(), null)
    }
  }

  abstract class FlippedPanel(w: Int, h: Int, backGroundColor: Color) extends Panel(w, h, backGroundColor) {
    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      g2d.translate(0, h)
      g2d.scale(1.0, -1.0)
    }
  }

  class DrawingPanel(width: Int, height: Int, backgroundColor: Color, boundedDrawingFunctionsC: BoundedDrawingFunction*) extends Panel(width, height, backgroundColor) {
    setOpaque(true)
    var boundedDrawingFunctions = boundedDrawingFunctionsC.toSeq

    def boundingBox = computeEnclosingBounds(boundedDrawingFunctions)

    def maxAxisBox = max(boundingBox.width, boundingBox.height)

    def minAxisPanel = min(getWidth, getHeight)

    def maxScale = max(getWidth/boundingBox.width, getHeight/boundingBox.height)

    def minScale = min(getWidth/boundingBox.width, getHeight/boundingBox.height)

    def scaleX = minScale

    def scaleY = minScale


    def transLateX = -scaleX * boundingBox.minX

    def transLateY = -scaleY * boundingBox.minY

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)
      doTransformations(g)
      boundedDrawingFunctions.foreach(fb => fb.drawingFunction(g2d))
    }

    def computeEnclosingBounds(bfs: Seq[BoundedDrawingFunction]): AABB = {
      val bs = bfs.map(_.bounds)
      val corners = bs.flatMap(b => b().corners)
      AABB(corners)
    }

    def doTransformations(g2d: Graphics2D): Unit = {
      g2d.translate(transLateX, transLateY)
      g2d.scale(scaleX, scaleY)
      g2d.setStroke(new BasicStroke(1 / scaleX))
    }


  }

  class FlippedDrawingPanel(width: Int, height: Int, backgroundColor: Color, drawingFunctions: BoundedDrawingFunction*) extends DrawingPanel(width, height, backgroundColor, drawingFunctions: _*) {
    override def paintComponent(g: Graphics) = {
      g.translate(0, h)
      g.scale(1.0, -1.0)
      super.paintComponent(g)
    }
  }

  class Frame(title: String, panels: Panel*) extends JFrame(title) {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    panels.foreach(add(_))
    pack()
    setVisible(true)
  }




}
