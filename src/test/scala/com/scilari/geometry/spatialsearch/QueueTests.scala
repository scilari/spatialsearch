package com.scilari.geometry.spatialsearch

import com.scilari.geometry.spatialsearch.queues.FloatMinHeap
import org.scalatest._
import flatspec._
import matchers._

class QueueTests extends AnyFlatSpec with should.Matchers {
  "FloatMinHeap" should "accept and return the elements in correct order" in {
    val heap = new FloatMinHeap[String](2)
    heap.enqueue(0.4f, "D")
    heap.enqueue(0.5f, "E")
    heap.enqueue(0.2f, "B")
    heap.enqueue(0.1f, "A")
    heap.enqueue(0.3f, "C")
    heap.enqueue(-0.4f, "d")
    heap.enqueue(-0.5f, "e")
    heap.enqueue(-0.2f, "b")
    heap.enqueue(-0.1f, "a")
    heap.enqueue(-0.3f, "c")


    heap.size should be (10)
    assert(heap.nonEmpty)

    val correctOrder = Seq("e", "d", "c", "b", "a", "A", "B", "C", "D", "E")
    val dequeued = for(_ <- 0 until heap.size) yield {
      heap.dequeueValue()
    }

    dequeued.zip(correctOrder).foreach{ case(s1, s2) =>
      s1 should be (s2)
    }

    heap.size should be (0)
    assert(heap.isEmpty)

  }
}


