package com.scilari.geometry.models

/**
 * An object that can intersect a half-plane. The half-plane is defined by the normal vector of the separating line
 * given by its endpoints.
 */
trait Support {
  def support(direction: Float2): Float2

  /**
   * Determines if the extreme point lies on a half-plane defined by two points (the difference representing the normal
   * and the starting point giving the border)
   *
   * @param normalEndPointInside     Point lying inside the half-plane
   * @param normalStartPointOnBorder Point lying on the border of the half-plane
   * @return
   */
  def intersectsHalfPlane(normalEndPointInside: Float2, normalStartPointOnBorder: Float2): Boolean = {
    val normal = normalStartPointOnBorder - normalEndPointInside
    val toBorder = normalStartPointOnBorder - support(normal)
    toBorder.dot(normal) >= 0
  }
}
