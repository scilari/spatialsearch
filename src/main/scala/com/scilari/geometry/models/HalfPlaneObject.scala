package com.scilari.geometry.models

/**
  * An object that can intersect a half-plane. The half-plane is defined by its normal vector's endpoints.
  */
trait HalfPlaneObject {
  def pointDeepestInHalfPlane(normal: Float2): Float2
  def intersectsHalfPlane(normalEndPointInside: Float2, normalStartPointOnBorder: Float2): Boolean  = {
    val normal = normalStartPointOnBorder - normalEndPointInside
    val toBorder = normalStartPointOnBorder - pointDeepestInHalfPlane(normal)
    toBorder.dot(normal) >= 0
  }
}
