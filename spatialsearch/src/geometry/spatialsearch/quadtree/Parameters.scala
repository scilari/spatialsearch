package com.scilari.geometry.spatialsearch.quadtree

import com.scilari.geometry.models.AABB

case class Parameters(
  nodeElementCapacity: Int = Parameters.defaultNodeCapacity,
  minNodeSize: Float = Parameters.defaultMinNodeSize
)

object Parameters{
  val defaultNodeCapacity = 23
  val defaultMinNodeSize = 0.0001f
  val defaultMaxDepth = 10

  val default: Parameters = Parameters()

  def computeMinNodeSize(root: AABB, maxDepth: Int): Float ={
    root.width * math.pow(2.0, -(maxDepth - 2).toDouble).toFloat
  }

  def apply(root: AABB): Parameters = apply(root, defaultMaxDepth)

  def apply(root: AABB, maxDepth: Int): Parameters =
    Parameters(defaultNodeCapacity, computeMinNodeSize(root, maxDepth))

}
