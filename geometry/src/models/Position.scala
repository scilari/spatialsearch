package com.scilari.geometry.models

trait Position {
  def position: Float2
}

object Position {
  import scala.language.implicitConversions

  implicit def PositionAsFloat2(p: Position): Float2 = p.position
}
