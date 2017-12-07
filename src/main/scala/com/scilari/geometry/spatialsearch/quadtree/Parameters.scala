package com.scilari.geometry.spatialsearch.quadtree

case class Parameters(
  nodeElementCapacity: Int = Parameters.defaultNodeCapacity,
  minNodeSize: Float = Parameters.defaultMinNodeSize
)

object Parameters{
  val defaultNodeCapacity = 63
  val defaultMinNodeSize = 0.000001f
}
