package com.scilari.geometry.renderer
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.CanvasRenderingContext2D
import com.scilari.geometry.models.{Float2, AABB}
import com.scilari.math.FloatMath._

class BoundedRenderer(
    canvas: Canvas,
    drawingFunctions: () => Unit,
    dynamicBounds: () => AABB = () => AABB.positiveSquare(200f)
) {

  val lineWidth = 1.0
  val blurCorrectionRatio = 4

  given ctx: CanvasRenderingContext2D =
    canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  BoundedRenderer.fixBlurring(canvas, blurCorrectionRatio)

  def render(): Unit = {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.save()
    ctx.transform(1, 0, 0, -1, 0, canvas.height)

    val bounds = dynamicBounds()
    val transform = BoundedRenderer.Transform(canvas, bounds)
    renderTransformed(transform)

    ctx.restore()
  }

  private def renderTransformed(
      transform: BoundedRenderer.Transform
  )(using ctx: CanvasRenderingContext2D): Unit = {
    ctx.save()
    ctx.translate(transform.translate.x, transform.translate.y)
    ctx.scale(transform.scale, transform.scale)
    ctx.lineWidth = blurCorrectionRatio * lineWidth / transform.scale
    drawingFunctions()
    ctx.restore()
  }

}

object BoundedRenderer {

  val lineWidth = 1.0f

  case class Transform(canvas: Canvas, bounds: AABB) {
    val (scale, translate) = {
      val tightAxisScale = min(canvas.width / bounds.width, canvas.height / bounds.height)
      val scale = tightAxisScale
      val enclosingCenteredTranslate = Float2(
        canvas.width / 2 - scale * (bounds.width / 2 + bounds.minX),
        canvas.height / 2 - scale * (bounds.height / 2 + bounds.minY)
      )
      (scale, enclosingCenteredTranslate)
    }
  }

  def fixBlurring(canvas: Canvas, blurCorrectionRatio: Int): Unit = {
    canvas.width *= blurCorrectionRatio
    canvas.height *= blurCorrectionRatio

    canvas.style.width = s"${canvas.width / blurCorrectionRatio}px"
    canvas.style.height = s"${canvas.height / blurCorrectionRatio}px"
  }
}
