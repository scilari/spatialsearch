package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.spatialsearch.core.{IncrementallySearchable, SearchState}
import com.scilari.geometry.spatialsearch.queues.FloatHeap

import scala.collection.mutable.ArrayBuffer

trait Knn extends IncrementallySearchable {
  var root: NodeType
  val k: Int

  override def initialState(q: Q): SearchState[Q, E, NodeType] = {
    new SearchState[Q, E, NodeType](
      q,
      FloatHeap[NodeType](0, root, 7),
      new FloatHeap[E](15),
      new ArrayBuffer[E](foundElemSizeHint)
    )
  }

  override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
  override val foundElemSizeHint: Int = k

}


