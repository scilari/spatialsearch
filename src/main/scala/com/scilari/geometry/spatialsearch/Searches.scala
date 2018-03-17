package com.scilari.geometry.spatialsearch


import com.scilari.geometry.models.MetricObject


import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by iv on 1/17/2017.
  */
trait Searches[P, E <: MetricObject[P]] extends IncrementallySearchable[P, E]{

  def knn(k: Int): SearchFn = search(new KnnParameters(k))

  def range(r: Float, sizeHint: Int = Searches.defaultRangeSizeHint): SearchFn = search(new RangeParameters(r, sizeHint))

  def knnWithCondition(k: Int, condition: E => Boolean): SearchFn = search(new KnnWithCondition(k, condition))

  def rangeUntilFirstFound(r: Float): SearchFn = search(new RangeUntilFirstFound(r))

  def removal(e: E): SearchFn = search(new Removal(e)) // TODO: does not belong here


  final class KnnParameters(k: Int) extends SearchParameters{
    override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
    override val foundElemSizeHint: Int = k
  }

  final class RangeParameters(r: Float, sizeHint: Int = Searches.defaultRangeSizeHint) extends SearchParameters{
    // Skipping the queues in favor of performance, as we do not need the elements in order
    override def modifyState(s: State): Unit = {
      Range.range(s.queryPoint, s.nodes.getValues, r, s.foundElements)
    }

    override def endCondition(s: State): Boolean = true
    override val elemQueueSizeHint: Int = 0
    override val nodeQueueSizeHint: Int = 1
    override val foundElemSizeHint: Int = sizeHint

  }

  object Range{
    def range(queryPoint: P, rootNodes: Seq[BaseType],
      r: Float, foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()): Seq[E] = {
      val rSq = r * r

      @tailrec
      def rangeRec(nodes: List[Base]): Unit ={
        nodes match{
          case (leaf: Leaf) :: (tail: List[_]) =>
            if(leaf.distanceSq(queryPoint) <= rSq){
              leaf.elements.foreach(e => if(elemDist(queryPoint, e) <= rSq) foundElements += e)
            }
            rangeRec(tail)
          case (node: Node) :: (tail: List[Base]) =>
            var ns = tail
            val cs = node.children
            var i = 0; val n = cs.length
            while (i < n) { if(nodeDist(queryPoint, cs(i)) <= rSq) ns = cs(i) :: ns; i += 1}
            rangeRec(ns)
          case _ => ()
        }
      }

      rangeRec(rootNodes.toList)
      foundElements
    }
  }

  class KnnWithCondition(k: Int, condition: E => Boolean) extends SearchParameters {
    override def endCondition(s: State): Boolean = s.foundElements.lengthCompare(k) >= 0
    override def filterElements(e: E, s: State): Boolean = condition(e)
  }


  class RangeUntilFirstFound(r: Float) extends SearchParameters{
    val rSq: Float = r*r
    override def endCondition(s: State): Boolean = {
      s.foundElements.nonEmpty || (s.headElemDist > rSq && s.headNodeDist > rSq)
    }
  }


  final class Removal(e: E) extends SearchParameters{
      override def filterNodes(n: BaseType, s: State): Boolean =
        n.zeroDistance(s.queryPoint)

      override def filterElements(e: E, s: State): Boolean =
        e.zeroDistance(s.queryPoint)

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

object Searches{
  val defaultRangeSizeHint: Int = 32
}
