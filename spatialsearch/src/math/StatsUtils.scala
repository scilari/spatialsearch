package com.scilari.math

object StatsUtils {

  def mean(xs: Seq[Double]): Double = xs.sum / xs.size

  def variance(xs: Seq[Double]): Double = {
    val m = mean(xs)
    val dxs = xs.map { x => (x - m) * (x - m) }
    mean(dxs)
  }

  def std(xs: Seq[Double]): Double = math.sqrt(variance(xs))
}
