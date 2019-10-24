package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.spatialsearch.core.Tree.Node
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

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
    val foundElems = new ArrayBuffer[E](8)

    @tailrec
    def rangeRec(nodes: List[NodeType]): Seq[E] = {
      if(nodes.isEmpty){
        foundElems
      } else {
        val h = nodes.head
        val t = nodes.tail

        if (h.isLeaf) {
          val es = h.elements
          val n = es.size; var i = 0
          while(i < n){
            val e = h.elements(i)
            if (elemDist(queryPoint, e) <= rSq) foundElems += e
            i += 1
          }
//          @tailrec
//          def rec(es: List[E]): Unit ={
//            if (es.nonEmpty) {
//              val t = es.tail
//              if (t.nonEmpty) {
//                val e = es.head
//                if (elemDist(queryPoint, e) <= rSq) foundElems = e :: foundElems
//                rec(t)
//              }
//            }
//          }

           // rec(h.elements)
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
              if (nodeDist(queryPoint, c) <= rSq) ns = c :: ns
              i += 1
            }
            rangeRecLeaves(ns, foundLeaves)
          }
        }
      }

    rangeRecLeaves(rootNodes, List[NodeType]())
  }

}
