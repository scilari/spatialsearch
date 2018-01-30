package com.scilari.geometry.plotting

import java.awt.{Color, Graphics2D}

import com.scilari.geometry.models.{AABB, DataPoint, Float2, Float3}
import com.scilari.geometry.plotting.Surface.ColorContainer
import com.scilari.math._

class Surface(data: Seq[DataPoint[Float3]], val valueMins: Float3 = Float3(0f), val valueMaxs: Float3 = Float3(1f)) {
  val normalizedData: Seq[DataPoint[ColorContainer]] = {
    data.map{p => normalizeData(p)}
  }

  def normalizeData(data: DataPoint[Float3]): DataPoint[ColorContainer] = {
    val normalized = data.data.clampNormalize(valueMins, valueMaxs)
    val color = new Color(normalized.x, normalized.y, normalized.z, 1f)
    new DataPoint[ColorContainer](data.x, data.y, new ColorContainer(color))
  }

  def setAlpha(alpha: Float): Unit ={
    normalizedData.foreach(_.data.setAlpha(alpha))
  }

  def paintPoints(color: Color = Color.RED, radius: Float = 0.1f)(g2d: Graphics2D): Unit ={
    normalizedData.foreach{ d => drawPoint(d, color, radius)(g2d) }
  }

  def paintData(pixelSize: Float)(g2d: Graphics2D): Unit = {
    normalizedData.foreach { d =>
      val color = d.data.color
      val radius = pixelSize/2
      drawFilledPoint(d, color, radius)(g2d)
    }
  }

  def paintData(pixelSize: Float, alpha: Float)(g2d: Graphics2D): Unit = {
    normalizedData.foreach { d =>
      d.data.setAlpha(alpha)
      val color = d.data.color
      val radius = pixelSize/2
      drawFilledPoint(d, color, radius)(g2d)
    }
  }

}

object Surface{
  class ColorContainer(var color: Color){
    var alpha: Float = color.getAlpha/255f
    var value: Float = 1f // for color mapping purposes
    def setAlpha(alpha: Float): Unit = {
      this.alpha = com.scilari.math.clamp(alpha, 0f, 1f)
      color = colorWithAlpha(color, this.alpha)
    }
    def setColor(c: Color): Unit = color = colorWithAlpha(c, alpha)
  }

  def apply(data: Seq[DataPoint[Float3]]): Surface = {
    val valueMins = Float3(
      data.minBy{_.data.x}.data.x,
      data.minBy{_.data.y}.data.y,
      data.minBy{_.data.z}.data.z
    )

    val valueMaxs = Float3(
      data.maxBy{_.data.x}.data.x,
      data.maxBy{_.data.y}.data.y,
      data.maxBy{_.data.z}.data.z
    )

    new Surface(data, valueMins, valueMaxs)
  }

  /**
    * Normalizes the data based on its deviation and mean and squashes it through tanh to
    * get values between -1 and 1.
    * @param data Data points containing three dimensional inner data
    * @param devScale How many deviations maps to 1 in tanh input
    * @return Surface containing the data squashed in this way
    */
  def tanhSquashedWithDev(data: Seq[DataPoint[Float3]], devScale: Float = 2.0f): Surface = {
    val xs = data.map{_.data.x}
    val ys = data.map{_.data.y}
    val zs = data.map{_.data.z}

    val mX = mean(xs)
    val mY = mean(ys)
    val mZ = mean(zs)

    val devs = Float3(deviation(xs), deviation(ys), deviation(zs))

    val dX = xs.map{_ - mX}
    val dY = ys.map{_ - mY}
    val dZ = zs.map{_ - mZ}

    val translated = (dX zip dY zip dZ).map{ case((x, y), z) => Float3(x, y, z) }

    val scaled: Seq[Float3] = translated.map{d => (d*devScale)/devs}

    val squashed = scaled.map{ d => Float3(Math.tanh(d.x), Math.tanh(d.y), Math.tanh(d.z))}

    val squashedData = squashed.indices.map{i => new DataPoint[Float3](data(i), squashed(i))}
    new Surface(squashedData, Float3(-1f), Float3(1f))

  }

  def tanhSquashed(data: Seq[DataPoint[Float3]], squashRatio: Float = 0.5f): Surface = {
    val xs = data.map{_.data.x}
    val ys = data.map{_.data.y}
    val zs = data.map{_.data.z}
    val max = Float3(xs.max, ys.max, zs.max)
    val min = Float3(xs.min, ys.min, zs.min)

    val normalized = data.map{d => d.data.clampNormalize(min, max)}

    val scaled = normalized.map{d => (d - 0.5f)*2f*squashRatio}

    val squashed = scaled.map{ d => Float3(Math.tanh(d.x), Math.tanh(d.y), Math.tanh(d.z))}

    val squashedData = squashed.indices.map{i => new DataPoint[Float3](data(i), squashed(i))}
    new Surface(squashedData, Float3(-1f), Float3(1f))
  }

  def apply(data: Seq[DataPoint[Float3]], boundSurface: Surface): Surface = {
    new Surface(data, boundSurface.valueMins, boundSurface.valueMaxs)
  }

  def apply(dataPoints: Seq[Float2], interpolant: Float2 => Float3): Surface ={
    val interpolated = dataPoints.map(p => new DataPoint[Float3](p.x, p.y, interpolant(p)))
    Surface(interpolated)
  }

  def surfaceGrid(box: AABB, pixelSize: Float): Seq[Float2] = {
    val halfSize = pixelSize/2
    for{
      x <- box.minX + halfSize until box.maxX by pixelSize
      y <- box.minY + halfSize until box.maxY by pixelSize
    } yield Float2(x,y)

  }
}