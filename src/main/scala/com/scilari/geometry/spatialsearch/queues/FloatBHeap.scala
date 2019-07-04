package com.scilari.geometry.spatialsearch.queues

class FloatBHeap[E](initialCapacity: Int) extends FloatHeap[E](initialCapacity){
  val pageShift: Int = 6
  val pageSize: Int = 1 << pageShift
  val pageMask: Int = pageSize - 1

  override def left(k: Int): Int = ???

  override def right(k: Int): Int = ???


  override def leftAndRight(k: Int, arr: Array[Int]): Unit = {
    val re = k & pageMask
    var L: Int = 0
    var R: Int = 0
    if(k > pageMask && re < 2) {
      L = k + 2
      R = L
    } else if((k & (pageSize >> 1)) != 0) {
      L = (k & ~pageMask) >> 1
      L |= k & (pageMask >> 1)
      L += 1
      L <<= pageShift
      R = L + 1
    } else {
      L = k + (k & pageMask)
      R = L + 1
    }

    arr(0) = L
    arr(1) = R
    //arr
  }

  override def parent(k: Int): Int = {
    val re = k & pageMask
    if(k < pageSize || re > 3){
      (k & ~pageMask) | (re >> 1)
    } else if(re < 2){
      var p = (k - pageSize) >> pageShift
      p += p & ~(pageMask >> 1)
      p |= pageSize / 2
      p
    } else {
      k - 2
    }

  }


}

object FloatBHeap{
  def apply[E](k: Float, e: E, initialCapacity: Int): FloatHeap[E] ={
    val h = new FloatBHeap[E](initialCapacity)
    h.enqueue(k, e)
    h
  }
}
