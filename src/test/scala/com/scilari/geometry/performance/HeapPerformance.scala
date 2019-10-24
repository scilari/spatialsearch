package com.scilari.geometry.performance

import com.scilari.geometry.spatialsearch.TestUtils.Timing.{BlackHole, compareTime}
import com.scilari.geometry.spatialsearch.queues.FloatHeap
import com.scilari.geometry.spatialsearch.queues.FloatIndexHeap
import org.scalatest.{FlatSpec, Matchers}

class HeapPerformance extends FlatSpec with Matchers {

  "FloatIndexHeap" should "be fast" in {
    val runCount = 10000
    val r = scala.util.Random
    val n = 1000
    val keys = (0 until n).map{ _ => r.nextFloat() }
    val indices = keys.indices.toArray
    val objects = Array.fill(n)(new Object)

    val iHeap = new FloatIndexHeap();
    val heap = new FloatHeap[Object]()

    val result = compareTime(
      "FloatIndexHeap", {
        for(i <- indices){
          val k = keys(i)
          iHeap(k, i)
          if(i % 5 == 0){
            BlackHole.consumeAny(objects(iHeap.dequeueValue()))
          }
        }
        iHeap.clear()
      },
      "FloatHeap", {
        for(i <- indices){
          val k = keys(i)
          val o = objects(i)
          heap(k, o)
          if(i % 5 == 0){
            BlackHole.consumeAny(heap.dequeueValue())
          }
        }
        heap.clear()
      },
      runCount
    )

    info("\n== Heap insertions and removal == ")
    info(result.toInfo(runCount * n))

  }




}
