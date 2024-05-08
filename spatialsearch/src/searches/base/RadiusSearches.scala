package com.scilari.geometry.spatialsearch.searches.base

import com.scilari.geometry.models.{Float2, Position}
import com.scilari.geometry.spatialsearch.core.SearchConfig.DistanceConfig
import com.scilari.geometry.spatialsearch.quadtree.Tree.Node

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

trait RadiusSearches[E <: Position](using DistanceConfig) {

  def initialNodes: List[Node[E]]
  def rangeSearch(queryPoint: Float2, r: Float): ArrayBuffer[E] =
    Radius[E](initialNodes, r).search(queryPoint)

  def rangeExcludeNode(queryPoint: Float2, r: Float, node: Node[E]): ArrayBuffer[E] =
    Radius[E](initialNodes, r).searchExcludeNode(queryPoint, node)

  def rangeSearchLeaves(queryPoint: Float2, r: Float): List[Node[E]] =
    Radius[E](initialNodes, r).searchLeaves(queryPoint)

  // TODO: optimize if possible
  def isEmptyRange(queryPoint: Float2, r: Float): Boolean = rangeSearch(queryPoint, r).isEmpty

  final class Radius[E <: Position](val initialNodes: List[Node[E]], r: Float)(using
      distanceConfig: DistanceConfig
  ) {

    val rSq: Float = r * r
    val defaultRangeSizeHint: Int = 32

    def search(queryPoint: Float2): ArrayBuffer[E] = {
      range(queryPoint, initialNodes, rSq)
    }

    def searchExcludeNode(queryPoint: Float2, node: Node[E]): ArrayBuffer[E] = {
      rangeExcludeNode(queryPoint, initialNodes, rSq, node)
    }

    def searchLeaves(queryPoint: Float2): List[Node[E]] = {
      rangeLeaves(queryPoint, initialNodes, rSq)
    }

    private def rangeExcludeNode(
        queryPoint: Float2,
        rootNodes: List[Node[E]],
        rSq: Float,
        node: Node[E]
    ): ArrayBuffer[E] = {
      val leaves = rangeLeaves(queryPoint, rootNodes, rSq)
      var foundElems = ArrayBuffer[E]()
      leaves.foreach { leaf =>
        if (leaf != node) {
          val es = leaf.elements
          val n = es.length
          var i = 0
          while (i < n) {
            val e: E = es(i)
            if (distanceConfig.elemDist(queryPoint, e) <= rSq) foundElems += e
            i += 1
          }
        }
      }
      foundElems
    }

    private def range(queryPoint: Float2, rootNodes: List[Node[E]], rSq: Float): ArrayBuffer[E] = {
      val leaves = rangeLeaves(queryPoint, rootNodes, rSq)
      var foundElems: ArrayBuffer[E] = ArrayBuffer[E]()
      leaves.foreach { leaf =>
        val es = leaf.elements
        val n = es.length
        var i = 0
        while (i < n) {
          val e: E = es(i)
          if (distanceConfig.elemDist(queryPoint, e) <= rSq) foundElems += e
          i += 1
        }
      }
      foundElems
    }

    private def rangeLeaves(
        queryPoint: Float2,
        rootNodes: List[Node[E]],
        rSq: Float
    ): List[Node[E]] = {

      @tailrec
      def rangeRecLeaves(nodes: List[Node[E]], foundLeaves: List[Node[E]]): List[Node[E]] = {
        if (nodes.isEmpty) {
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
              if (distanceConfig.nodeDist(queryPoint, c.bounds) <= rSq) ns ::= c
              i += 1
            }
            rangeRecLeaves(ns, foundLeaves)
          }
        }
      }

      rangeRecLeaves(rootNodes, List[Node[E]]())
    }

  }

}
