package com.scilari.geometry.spatialsearch


import com.scilari.geometry.models.{Float2, HalfPlaneObject, MetricObject}
import com.scilari.geometry.spatialsearch.IncrementallySearchable.{SearchParameters, State}
import com.scilari.math._

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by iv on 1/17/2017.
  */
object Searches {


  final class Knn[P, E <: MetricObject[P]](k: Int) extends IncrementallySearchable[P, E]{
    val parameters: SearchParameters[P, E] = KnnParameters
    private final object KnnParameters extends SearchParameters[P, E]{
      override def endCondition(s: State[P, E]): Boolean = s.foundElements.size >= k
      override val foundElemSizeHint: Int = k
    }
  }

  final class Range[P, E <: MetricObject[P]](r: Float, sizeHint: Int)
    extends IncrementallySearchable[P, E]{
    val parameters: SearchParameters[P, E] = RangeParameters

    private final object RangeParameters extends SearchParameters[P, E]{
      // Skipping the queues in favor of performance, as we do not need the elements in order
      override def modifyState(s: State[P, E]): Unit =
        Range.range[P, E](s.queryPoint, s.nodes.dequeueValue(), r, s.foundElements)

      override val elemQueueSizeHint: Int = 0
      override val nodeQueueSizeHint: Int = 1
      override val foundElemSizeHint: Int = sizeHint

    }
  }

  object Range{
    def range[P, E <: MetricObject[P]](queryPoint: P, initialNode: Tree[P, E]#BaseType,
      r: Float, foundElements: mutable.Buffer[E] = new ArrayBuffer[E]()): Seq[E] = {
      val rSq = r * r
      var nodes = List(initialNode)
      //val nodes = ArrayBuffer(initialNode)
      //nodes += initialNode
      while (nodes.nonEmpty) {
        val node = nodes.head
        nodes = nodes.tail
        //nodes.trimEnd(1)
        node match {
          case leaf: Tree[P, E]#Leaf =>
            leaf.elements.foreach(e => if (e.distanceSq(queryPoint) <= rSq) foundElements += e)
          case node: Tree[P, E]#Node =>
            node.children.foreach(n => if (n.distanceSq(queryPoint) <= rSq) nodes = n :: nodes)
        }
      }
      foundElements
    }

  }

  class KnnWithCondition[P, E <: MetricObject[P]](k: Int, condition: E => Boolean)
    extends IncrementallySearchable[P, E]{
    val parameters: SearchParameters[P, E] = new SearchParameters[P, E] {
      override def endCondition(s: State[P, E]): Boolean = s.foundElements.size >= k
      override def filterElements(e: E, s: State[P, E]): Boolean = condition(e)
    }
  }

  class RangeUntilFirstFound[P, E <: MetricObject[P]](r: Float)
    extends IncrementallySearchable[P, E]{
    val rSq: Float = r*r
    val parameters: SearchParameters[P, E] = new SearchParameters[P, E] {
      override def endCondition(s: State[P, E]): Boolean = {
        s.foundElements.nonEmpty || (s.elemDistSq > rSq && s.nodeDistSq > rSq)
      }
    }
  }

  final class Polygonal[P <: Float2, E <: Float2 with MetricObject[P]] extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters[P, E] = PolygonalParameters

    private final object PolygonalParameters extends SearchParameters[P, E]{
      override def filterElements(e: E, s: State[P, E]): Boolean = {
        !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean = {
        !isDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }

  final class PolygonalMaxRange[P <: Float2, E <: MetricObject[P] with Float2](r: Float) extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters[P, E] = PolygonalParameters
    private[this] val rSq = r*r

    private[this] final object PolygonalParameters extends SearchParameters[P, E]{

      override def filterElements(e: E, s: State[P, E]): Boolean = {
        s.queryPoint.distanceSq(e) <= rSq && !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean = {
        n.distanceSq(s.queryPoint) <= rSq && !isDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }


  object Polygonal{
    def isDominatedBy(e: HalfPlaneObject, queryPoint: Float2, dominator: Float2): Boolean ={
      !e.intersectsHalfPlane(queryPoint, dominator)
    }

    def isDominatedBy(e: HalfPlaneObject, queryPoint: Float2, dominators: Seq[Float2]): Boolean = {
      // Going through in reverse order, as more recently added points are more likely to dominate
      var i = dominators.size - 1
      while(i >= 0){
        if(isDominatedBy(e, queryPoint, dominators(i))) return true
        i -= 1
      }
      false
    }
  }

  final class PolygonalDynamicMaxRange[P <: Float2, E <: Float2 with MetricObject[P]](maxRangeFactor: Float) extends IncrementallySearchable[P, E]{
    import Polygonal._
    val parameters: SearchParameters[P, E] = PolygonalParameters
    private[this] val rangeFactorSq = maxRangeFactor*maxRangeFactor
    private[this] var firstElementDistSq = Float.PositiveInfinity
    private[this] var maxRange = Float.PositiveInfinity


    private[this] final object PolygonalParameters extends SearchParameters[P, E]{
      override def modifyState(s: State[P, E]): Unit = {
        if(firstElementDistSq > s.elemDistSq){
          firstElementDistSq = s.elemDistSq
          maxRange = firstElementDistSq*rangeFactorSq
        }
      }

      override def filterElements(e: E, s: State[P, E]): Boolean = {
        e.distanceSq(s.queryPoint) <= maxRange && !isDominatedBy(e, s.queryPoint, s.foundElements)
      }

      override def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean = {
        n.distanceSq(s.queryPoint) <= maxRange && !isDominatedBy(n, s.queryPoint, s.foundElements)
      }

      override val foundElemSizeHint: Int = 8
    }
  }



  final class Removal[P, E <: MetricObject[P]](e: E)
    extends IncrementallySearchable[P, E]{

    val parameters: SearchParameters[P, E] = new SearchParameters[P, E] {
      override def filterNodes(n: Tree[P, E]#BaseType, s: State[P, E]): Boolean =
        n.zeroDistance(s.queryPoint)

      override def filterElements(e: E, s: State[P, E]): Boolean =
        e.zeroDistance(s.queryPoint)

      override def modifyState(s: State[P, E]): Unit = {
        //debugState(s)
        if(s.nodeDistSq == 0)
          s.nodes.head.value match{
            case leaf: Tree[P, E]#Leaf =>
              leaf.elements -= e

            case _ => ()
          }
      }
    }
  }




  def debugState(state: State[_, _]): Unit ={
    println("Node queue length: " + state.nodes.size + ", closest at: " + sqrt(state.nodeDistSq))
    println("Elem queue length: " + state.elements.size + ", closest at: " + sqrt(state.elemDistSq))
    println("Found elements: " + state.foundElements.size)
  }

}
