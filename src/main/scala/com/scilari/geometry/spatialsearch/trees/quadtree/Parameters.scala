package com.scilari.geometry.spatialsearch.trees.quadtree


import com.scilari.geometry.models.AABB

case class Parameters(
  nodeElementCapacity: Int = Parameters.defaultNodeCapacity,
  minNodeSize: Float = Parameters.defaultMinNodeSize
)

object Parameters{
  val defaultNodeCapacity = 63
  val defaultMinNodeSize = 0.000001f
  val defaultMaxDepth = 10

  def computeMinNodeSize(root: AABB, maxDepth: Int): Float ={
    root.width * math.pow(2, -(maxDepth - 2)).toFloat
  }

  def apply(root: AABB): Parameters = apply(root, defaultMaxDepth)

  def apply(root: AABB, maxDepth: Int): Parameters =
    Parameters(defaultNodeCapacity, computeMinNodeSize(root, maxDepth))


}
