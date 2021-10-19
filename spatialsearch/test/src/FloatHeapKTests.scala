package com.scilari.geometry.spatialsearch

import com.scilari.geometry.spatialsearch.TestUtils.Timing.{BlackHole, warmUpAndMeasureTime}
import com.scilari.geometry.spatialsearch.queues.{FloatMinHeap, FloatMaxHeapK}
import org.scalatest._
import flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class FloatHeapKTests extends AnyFlatSpec with Matchers{

  "FloatHeapK" should "find k smallest elements" in {
    val k = 5
    val keys = (0 until 100) map { _ => Random.nextDouble().toFloat }
    val heap = new FloatMaxHeapK[Float](k)
    keys.foreach { key =>
      heap.enqueue(key, key)
    }

    heap.peekValues.toSet equals keys.sorted.take(k).toSet
  }

  it should "be faster than full heap with extreme heap sizes" in {
    val runCount = 1000
    val elemCount = 100
    val k = 5

    val t1 = warmUpAndMeasureTime({
      val heap = new FloatMaxHeapK[Float](k)
      (0 until elemCount).foreach { _ =>
        val key = Random.nextDouble().toFloat
        heap.enqueue(key, key)
      }
      BlackHole.consumeAny(heap.peekValues)

    }, runCount, warmUpCount = 500)

    val t2 = warmUpAndMeasureTime({
      val heap = new FloatMinHeap[Float](initialCapacity = elemCount)
      (0 until elemCount).foreach { _ =>
        val key = Random.nextDouble().toFloat
        heap.enqueue(key, key)
      }
      (0 until k).foreach{_ =>
        BlackHole.consumeAny(heap.dequeueValue())
      }

    }, runCount, warmUpCount = 500)

    info("K-optimized heap time: " + t1)
    info("Standard heap time: " + t2)
  }

}