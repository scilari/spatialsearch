package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.Float2

trait Searches[P <: Float2, E <: Float2] extends BasicSearches[P, E] with PolygonalSearches[P, E]
