package com.scilari.geometry.spatialsearch.plotting
import java.awt.{Color, Graphics2D}
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import com.scilari.geometry.models.{AABB, DataPoint, Float2}
import com.scilari.geometry.spatialsearch.TestResources._
import com.scilari.geometry.plotting.Panels.{FlippedDrawingPanel, Frame}
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

/**
  * Test visualization with Finnish city population data
  * Created by Ilari.Vallivaara on 1/20/2017.
  */
object DrawFinland extends App{
  val pixelSize = 1 // (km)
  val paintRadius = 50 // (km)
  val panelW = 1000
  val margin = 100f


  val box = AABB.EnclosingSquare(cityData, margin = margin)
  val cityTree = QuadTree(cityData)

  println(box)

  val grid = surfaceGrid(box, pixelSize)

  val t0 = System.currentTimeMillis()

  def colorMap(p: Float2, tree: QuadTree[DataPoint[City]]): DataPoint[Color] = {
    val cities = tree.rangeSearch(p, paintRadius)
    val popSum = cities.map{_.data.population}.sum
    val color = if(cities.isEmpty){
      Color.BLACK
    } else if(popSum < 50000){
      Color.RED
    } else if(popSum < 150000){
      Color.YELLOW
    } else {
      Color.GREEN
    }

    new DataPoint(p, color)
  }

  def surfaceGrid(box: AABB, pixelSize: Float): Seq[Float2] = {
    val halfSize = pixelSize/2
    for{
      x <- box.minX + halfSize until box.maxX by pixelSize
      y <- box.minY + halfSize until box.maxY by pixelSize
    } yield Float2(x, y)
  }


  val colors = grid.map{ p => colorMap(p, cityTree) }


  val t = System.currentTimeMillis() - t0
  println("Evaluating " + colors.size + " pixels took " + t + " milliseconds (" + t.toDouble/colors.size + " ms/px).")


  val animatedTree = QuadTree[DataPoint[City]](box)
  var animatedImg = new BoxToBufferedImage(box, pixelSize)

  val pixBox = AABB(0, 0, animatedImg.imageWidth, animatedImg.imageHeight)
  val animatedPanel = new FlippedDrawingPanel(panelW, panelW, Color.BLACK, (animatedImg.drawBitmap _, pixBox))
  val animatedFrame = new Frame("Finland: Adding data slowly", animatedPanel)


  val evalTimes = for(city <- cityData.sortBy(c => c.data.name)) yield {

    val t0 = System.currentTimeMillis()

    animatedTree.add(city)
    val newGrid = surfaceGrid(new AABB(center = city, halfWidth = paintRadius), pixelSize)
      .filter(_.distance(city) <= paintRadius )

    newGrid.foreach{ p => animatedImg.updatePixel(p, colorMap(p, animatedTree).data) }

    val t = System.currentTimeMillis() - t0
    Thread.sleep(100)
    animatedPanel.validate()
    animatedPanel.repaint()
    t
  }

  println("Mean evaluation time per added city: " + evalTimes.sum.toDouble/evalTimes.size)

  ImageIO.write(animatedImg.image, "png", new File("images/finland.png"))

  class BoxToBufferedImage(val bb: AABB, val pixelSize: Float){
    val imageWidth: Int = (bb.width/pixelSize).toInt
    val imageHeight: Int = (bb.height/pixelSize).toInt
    val scaleBBtoImg = Float2(imageWidth/bb.width, imageHeight/bb.height)
    val image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
    println("Image dimensions: " + imageWidth + " " + imageHeight)

    def updatePixel(p: Float2, color: Color): Unit ={
      val p0 = p - bb.minPoint
      val pSc = p0 * scaleBBtoImg
      val i = pSc.x.toInt
      val j = pSc.y.toInt
      image.setRGB(i, j, color.getRGB)
    }


    def drawBitmap(g2d: Graphics2D): Unit = {
      g2d.drawImage(image,
        0, 0, imageWidth, imageHeight,
        null)
    }

  }



}
