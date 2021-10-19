package com.scilari.geometry.spatialsearch.queues

import collection.mutable.Buffer



/**
 * Optimized version for computing k smallest values from a stream of n >> k values. Skips enqueue operations when the
 * queue is full and the inserted element is larger than the current max.
 * @param k The number of smallest values we want to compute.
 * @tparam E Element type parameter.
 */
class FloatMaxHeapK[E](k: Int) extends FloatMinHeap[E](k){
  override def cmp(key1: Float, key2: Float): Boolean = key1 > key2
  
  override def enqueue(key: Float, value: E): Unit = {
    if(size + 1 < capacity){
      super.enqueue(key, value)
    } else if(key < headKey){
      bubbleDown(key, value)
    } // else just discard
  }
  
  override def maxKey = headKey
  
  override def minKey = keys.min
  
  // Use only peekValues
  override def dequeue(): FloatKey[E] = ???

  override def dequeueValue(): E = ???

  // should never happen
  override protected def doubleCapacity(): Unit = ???

}
