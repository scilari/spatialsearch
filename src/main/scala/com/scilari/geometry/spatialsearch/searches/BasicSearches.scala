package com.scilari.geometry.spatialsearch.searches

import com.scilari.geometry.spatialsearch.IncrementallySearchable

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by iv on 1/17/2017.
  */
trait BasicSearches[P, E] extends IncrementallySearchable[P, E]{

  protected def knn(k: Int): SearchFn = search(new KnnParameters(k))

  protected def range(r: Float, sizeHint: Int = Range.defaultRangeSizeHint): SearchFn = search(new RangeParameters(r, sizeHint))

  protected def knnWithCondition(k: Int, condition: E => Boolean): SearchFn = search(new KnnWithCondition(k, condition))

  protected def rangeUntilFirstFound(r: Float): SearchFn = search(new RangeUntilFirstFound(r))

  protected def removal(e: E): SearchFn = search(new Removal(e)) // TODO: does not belong here

  protected final class KnnParameters(k: Int) extends SearchParameters{
    override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
    override val foundElemSizeHint: Int = k
  }

  protected final class RangeParameters(r: Float, sizeHint: Int = Range.defaultRangeSizeHint) extends SearchParameters{
    // Skipping the queues in favor of performance, as we do not need the elements in order
    override def modifyState(s: State): Unit = {
      Range.range(s.queryPoint, s.nodes.getValues, r, s.foundElements)
    }

    override def endCondition(s: State): Boolean = true
    override val elemQueueSizeHint: Int = 0
    override val nodeQueueSizeHint: Int = 1
    override val foundElemSizeHint: Int = sizeHint

  }

  protected final class KnnWithCondition(k: Int, condition: E => Boolean) extends SearchParameters {
    override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
    override def filterElements(e: E, s: State): Boolean = condition(e)
  }


  protected final class RangeUntilFirstFound(r: Float) extends SearchParameters{
    val rSq: Float = r*r
    override def endCondition(s: State): Boolean = {
      s.foundElements.nonEmpty || (s.headElemDist > rSq && s.headNodeDist > rSq)
    }
  }

  private object Range{
    val defaultRangeSizeHint: Int = 32

    def range(queryPoint: P, rootNodes: Seq[BaseType],
      r: Float, foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()): Seq[E] = {
      val rSq = r * r

      @tailrec
      def rangeRec(nodes: List[Base]): Unit ={
        nodes match{
          case (leaf: Leaf) :: (tail: List[_]) =>
            val es = leaf.elements
            var i = 0; val n = es.size
            while(i < n){
              if(elemDist(queryPoint, es(i)) <= rSq) foundElements += es(i)
              i += 1
            }
            rangeRec(tail)
          case (node: Node) :: (tail: List[Base]) =>
            var ns = tail
            val cs = node.children
            var i = 0; val n = cs.length
            while (i < n) {
              if(nodeDist(queryPoint, cs(i)) <= rSq) ns = cs(i) :: ns; i += 1
            }
            rangeRec(ns)
          case _ => ()
        }
      }

      rangeRec(rootNodes.toList)
      foundElements
    }
  }

  protected final class Removal(e: E) extends SearchParameters{
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


