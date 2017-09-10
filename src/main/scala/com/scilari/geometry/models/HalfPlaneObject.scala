package com.scilari.geometry.models

/**
  * An object that can intersect a half-plane. The half-plane is defined by the normal vector of the separating line
  * given by its endpoints.
  */
trait HalfPlaneObject {
  def pointDeepestInHalfPlane(normal: Float2): Float2
  def intersectsHalfPlane(normalEndPointInside: Float2, normalStartPointOnLine: Float2): Boolean  = {
    val normal = normalStartPointOnLine - normalEndPointInside
    val toBorder = normalStartPointOnLine - pointDeepestInHalfPlane(normal)
    toBorder.dot(normal) >= 0
  }
}
