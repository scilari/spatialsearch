package com.scilari.math

object StatsUtils {

  def mean(xs: Iterable[Double]): Double = xs.sum / xs.size

  def variance(xs: Iterable[Double]): Double = {
    val m = mean(xs)
    val dxs = xs.map { x => (x - m) * (x - m) }
    mean(dxs)
  }

  def std(xs: Iterable[Double]): Double = math.sqrt(variance(xs))
}
