package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.{SearchableContainer}
import com.scilari.geometry.spatialsearch.searches.base.{KnnSearches, RadiusSearches}
import scala.collection.mutable.ArrayBuffer

import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig.manhattan

abstract class ManhattanSearches[E <: Position] extends RadiusSearches[E], KnnSearches[E]
