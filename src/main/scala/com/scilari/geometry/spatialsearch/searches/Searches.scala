package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.core.Tree


trait Searches[P, E <: Float2] extends Tree[E] {
  val basicSearches: BasicSearches[P, E]
  val polygonalSearches: PolygonalSearches[E]
  val seqSearches: BasicSearches[Array[P], E]
}




  
