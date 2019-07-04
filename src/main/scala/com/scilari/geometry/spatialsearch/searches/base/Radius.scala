package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.spatialsearch.core.Tree.{Branch, Leaf, Node}
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Radius extends DistanceConfig {
  var root: NodeType
  def rSq: Float
  val defaultRangeSizeHint: Int = 32
  type NodeType <: Node[E, NodeType]

  def search(queryPoint: Q): Seq[E] = {
    range(queryPoint, List(root), rSq)
  }

  private def range(queryPoint: Q, rootNodes: List[NodeType],
    rSq: Float, foundElements: mutable.Buffer[E] = new ArrayBuffer[E](defaultRangeSizeHint)): Seq[E] = {

    @tailrec
    def rangeRec(nodes: List[NodeType]): Unit = {
      if(nodes.nonEmpty){
        val h = nodes.head
        val t = nodes.tail

        if (h.isLeaf) {
          val es = h.elements
          var i = 0
          val n = es.size
          while (i < n) {
            val e = es(i)
            if (elemDist(queryPoint, e) <= rSq) foundElements += e
            i += 1
          }
          rangeRec(t)
        } else {
          var ns = t
          val cs = h.children
          var i = 0
          val n = cs.length
          while (i < n) {
            val c = cs(i)
            if (nodeDist(queryPoint, c) <= rSq) ns = c :: ns
            i += 1
          }
          rangeRec(ns)
        }
      }
    }

    rangeRec(rootNodes)
    foundElements
  }

}
