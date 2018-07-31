package com.scilari.geometry.spatialsearch

/**
  * Created by Ilari.Vallivaara on 1/18/2017.
  */
object TestUtils {
  def readCsvRows(filename: String): List[Array[String]] ={
    val bufferedSource = io.Source.fromFile(filename)
    val lines = for (line <- bufferedSource.getLines) yield {
      line.split(",").map(_.trim)
    }
    val linesAsList = lines.toList
    bufferedSource.close
    linesAsList

  }

  def readCsvColumns(filename: String): List[List[String]] = {
    readCsvRows(filename).transpose
  }

  object Timing {
    def nanosToMillis(nanos: Long): Double = nanos / 1e6

    // This is simple and probably flawed way to micro benchmark anything but gives consistent results with Google Caliper
    // (may as well be flawed too).
    // TODO: consider rewriting the benchmarks using http://openjdk.java.net/projects/code-tools/jmh/
    def measureTime[T](block: => T, count: Int = 1): Double = {
      val t0 = System.nanoTime()
      for (i <- 0 until count) {
        val dummy: T = block
        BlackHole.consumeAny(dummy)
      }
      nanosToMillis(System.nanoTime() - t0)
    }

    def measureTimeWithInit[T, U](initBlock: => T, block: => U, count: Int = 1): Double ={
      var tSum = 0L
      for(i <- 0 until count){
        val dummy1: T = initBlock
        BlackHole.consumeAny(dummy1)
        val t0 = System.nanoTime()
        val dummy2: U = block
        BlackHole.consumeAny(dummy2)
        tSum += System.nanoTime() - t0
      }
      nanosToMillis(tSum)

    }

    val defaultWarmupCount = 1

    def warmUpAndMeasureTimeWithInit[T, U](initBlock: => T, block: => U, count: Int = 1, warmUpCount: Int = defaultWarmupCount): Double = {
      for (i <- 0 until warmUpCount) BlackHole.consumeDouble(measureTimeWithInit(initBlock, block, count))
      measureTimeWithInit(initBlock, block, count)
    }



    def warmUpAndMeasureTime(block: => Any, count: Int, warmUpCount: Int = defaultWarmupCount): Double = {
      for (i <- 0 until warmUpCount) BlackHole.consumeDouble(measureTime(block, count))
      measureTime(block, count)
    }

    // naive black hole implementation
    object BlackHole {
      var state: Double = 0
      var anyState: Any = _

      def consume(array: Array[Double]): Unit = {
        state = array(0)
      }

      def consumeDouble(value: Double): Unit = {
        state = value
      }

      def consumeAny(value: Any): Unit = {
        anyState = value
      }
    }


    def similarTime(t1: Double, t2: Double, similarityRatio: Double = 3): Boolean ={
      t1/t2 < similarityRatio && t2/t1 < similarityRatio
    }

    def similarOrBetterTime(better: Double, baseLine: Double, similarityRatio: Double = 3): Boolean = {
      better/baseLine < similarityRatio
    }


  }

}
