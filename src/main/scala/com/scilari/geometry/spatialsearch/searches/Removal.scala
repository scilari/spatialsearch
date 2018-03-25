package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.spatialsearch.core.IncrementallySearchable

trait Removal[P, E] extends IncrementallySearchable[P, E]{
  protected def removal(e: E): SearchFn = search(new RemovalParameters(e))

  protected final class RemovalParameters(e: E) extends SearchParameters{
    override def filterNodes(n: BaseType, s: State): Boolean =
      nodeDist(s.queryPoint, n) <= 0f

    override def filterElements(e: E, s: State): Boolean =
      elemDist(s.queryPoint, e) <= 0f

    override def modifyState(s: State): Unit = {
      if(s.headNodeDist == 0) {
        s.nodes.head.value match {
          case leaf: Leaf => leaf.elements -= e
          case _ => ()
        }
      }
    }
  }

}
