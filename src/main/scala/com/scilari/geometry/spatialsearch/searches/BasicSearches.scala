package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.spatialsearch.core.IncrementallySearchable

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait BasicSearches[P, E] extends IncrementallySearchable[P, E]{

  def knn(k: Int): SearchFn = search(new KnnParameters(k))

  def range(r: Float, sizeHint: Int = Range.defaultRangeSizeHint): SearchFn = search(new RangeParameters(r, sizeHint))

  def knnWithCondition(k: Int, condition: E => Boolean): SearchFn = search(new KnnWithCondition(k, condition))

  def rangeUntilFirstFound(r: Float): SearchFn = search(new RangeUntilFirstFound(r))

  final class KnnParameters(k: Int) extends SearchParameters{
    override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
    override val foundElemSizeHint: Int = k
  }

  final class RangeParameters(r: Float, sizeHint: Int = Range.defaultRangeSizeHint) extends SearchParameters{
    // Skipping the queues in favor of performance, as we do not need the elements in order
    override def modifyState(s: State): Unit = {
      Range.range(s.queryPoint, s.nodes.getValues, r, s.foundElements)
    }

    override def endCondition(s: State): Boolean = true
    override val elemQueueSizeHint: Int = 0
    override val nodeQueueSizeHint: Int = 1
    override val foundElemSizeHint: Int = sizeHint

  }

  final class KnnWithCondition(k: Int, condition: E => Boolean) extends SearchParameters {
    override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
    override def filterElements(e: E, s: State): Boolean = condition(e)
  }


  final class RangeUntilFirstFound(r: Float) extends SearchParameters{
    val rSq: Float = r*r
    override def endCondition(s: State): Boolean = {
      s.foundElements.nonEmpty || (s.headElemDist > rSq && s.headNodeDist > rSq)
    }
    override val elemQueueSizeHint: Int = 1
    override val foundElemSizeHint: Int = 1
  }

  private object Range{
    val defaultRangeSizeHint: Int = 32

    def range(queryPoint: P, rootNodes: Seq[NodeType],
      r: Float, foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()): Seq[E] = {
      val rSq = r * r

      @tailrec
      def rangeRec(nodes: List[Node]): Unit ={
        nodes match{
          case (leaf: Leaf) :: (tail: List[Node]) =>
            leaf.elements.foreach(e => if(elemDist(queryPoint, e) <= rSq) foundElements += e)
            rangeRec(tail)
          case (node: Branch) :: (tail: List[Node]) =>
            var ns = tail
            val cs = node.children
            var i = 0; val n = cs.length
            while (i < n) {
              val c = cs(i)
              if(nodeDist(queryPoint, c) <= rSq) ns = c :: ns
              i += 1
            }
            rangeRec(ns)
          case _ => ()
        }
      }

      rangeRec(rootNodes.toList)
      foundElements
    }
  }


}


