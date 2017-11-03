package com.scilari.geometry.spatialsearch


import com.scilari.geometry.models.MetricObject


import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by iv on 1/17/2017.
  */
trait Searches[P, E <: MetricObject[P]]{

  final class Knn(k: Int) extends IncrementallySearchable[P, E]{
    val parameters: SearchParameters = KnnParameters
    private final object KnnParameters extends SearchParameters{
      override def endCondition(s: State): Boolean = s.foundElements.size >= k
      override val foundElemSizeHint: Int = k
    }
  }

  final class Range(r: Float, sizeHint: Int)
    extends IncrementallySearchable[P, E]{
    val parameters: SearchParameters = RangeParameters

    private final object RangeParameters extends SearchParameters{
      // Skipping the queues in favor of performance, as we do not need the elements in order
      override def modifyState(s: State): Unit =
        Range.range(s.queryPoint, s.nodes.dequeueValue(), r, s.foundElements)

      override val elemQueueSizeHint: Int = 0
      override val nodeQueueSizeHint: Int = 1
      override val foundElemSizeHint: Int = sizeHint

    }
  }

  object Range{
    def range(queryPoint: P, rootNode: Tree[P, E]#BaseType,
      r: Float, foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()): Seq[E] = {
      val rSq = r * r

      @tailrec
      def rangeRec(nodes: List[Tree[P, E]#BaseType]): Unit ={
        nodes match{
          case (leaf: Tree[P, E]#Leaf) :: (tail: List[_]) =>
            if(leaf.distanceSq(queryPoint) <= rSq)
              leaf.elements.foreach(e => if(e.distanceSq(queryPoint) <= rSq) foundElements += e)
            rangeRec(tail)
          case (node: Tree[P, E]#Node) :: (tail: List[Tree[P, E]#Base]) =>
            var ns = tail
            if(node.distanceSq(queryPoint) <= rSq) {
              var i = 0; val n = node.children.length
              while (i < n) { ns = node.children(i) :: ns; i += 1}
            }
            rangeRec(ns)
          case _ => ()
        }
      }

      rangeRec(List(rootNode))
      foundElements
    }

  }

  class KnnWithCondition(k: Int, condition: E => Boolean)
    extends IncrementallySearchable[P, E]{
    val parameters: SearchParameters = new SearchParameters {
      override def endCondition(s: State): Boolean = s.foundElements.size >= k
      override def filterElements(e: E, s: State): Boolean = condition(e)
    }
  }

  class RangeUntilFirstFound(r: Float)
    extends IncrementallySearchable[P, E]{
    val rSq: Float = r*r
    val parameters: SearchParameters = new SearchParameters {
      override def endCondition(s: State): Boolean = {
        s.foundElements.nonEmpty || (s.elemDistSq > rSq && s.nodeDistSq > rSq)
      }
    }
  }


  final class Removal(e: E)
    extends IncrementallySearchable[P, E]{

    val parameters: SearchParameters = new SearchParameters{
      override def filterNodes(n: Tree[P, E]#BaseType, s: State): Boolean =
        n.zeroDistance(s.queryPoint)

      override def filterElements(e: E, s: State): Boolean =
        e.zeroDistance(s.queryPoint)

      override def modifyState(s: State): Unit = {
        if(s.nodeDistSq == 0)
          s.nodes.head.value match{
            case leaf: Tree[P, E]#Leaf =>
              leaf.elements -= e

            case _ => ()
          }
      }
    }
  }



}
