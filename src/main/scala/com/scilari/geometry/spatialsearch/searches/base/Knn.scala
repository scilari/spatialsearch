package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.spatialsearch.core.SearchState.DefaultInitialState
import com.scilari.geometry.spatialsearch.core.{IncrementallySearchable}


trait Knn extends IncrementallySearchable with DefaultInitialState {
  var root: NodeType
  val k: Int

  override def endCondition(s: State): Boolean = s.foundElements.length >= k
  override val foundElemSizeHint: Int = k
}


