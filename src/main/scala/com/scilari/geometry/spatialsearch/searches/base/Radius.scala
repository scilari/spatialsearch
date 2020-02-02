package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.spatialsearch.core.Tree.Node
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

import scala.annotation.tailrec

trait Radius extends DistanceConfig {
  var root: NodeType
  def rSq: Float
  val defaultRangeSizeHint: Int = 32
  type NodeType <: Node[E, NodeType]

  def search(queryPoint: Q): Seq[E] = {
    range(queryPoint, List(root), rSq)
  }

  def searchLeaves(queryPoint: Q): Seq[NodeType] = {
    rangeLeaves(queryPoint, List(root), rSq)
  }

  private def range(queryPoint: Q, rootNodes: List[NodeType], rSq: Float): Seq[E] = {
    val leaves = rangeLeaves(queryPoint, rootNodes, rSq)
    var foundElems: List[E] = Nil
    leaves.foreach{ leaf =>
      val es = leaf.elements
      val n = es.length
      var i = 0
      while (i < n) {
        val e: E = es(i)
        if(elemDist(queryPoint, e) <= rSq) foundElems ::= e
        i += 1
      }
    }
    foundElems
  }

  private def rangeLeaves(queryPoint: Q, rootNodes: List[NodeType], rSq: Float): Seq[NodeType] = {

    @tailrec
    def rangeRecLeaves(nodes: List[NodeType], foundLeaves: List[NodeType]): List[NodeType] = {
      if(nodes.isEmpty) {
        foundLeaves
      } else {
          val h = nodes.head
          val t = nodes.tail

          if (h.isLeaf) {
            rangeRecLeaves(t, h :: foundLeaves)
          } else {
            var ns = t
            val cs = h.children
            var i = 0
            val n = cs.length
            while (i < n) {
              val c = cs(i)
              if (nodeDist(queryPoint, c) <= rSq) ns ::= c
              i += 1
            }
            rangeRecLeaves(ns, foundLeaves)
          }
        }
      }

    rangeRecLeaves(rootNodes, List[NodeType]())
  }

}
