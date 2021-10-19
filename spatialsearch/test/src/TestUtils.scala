package com.scilari.geometry.spatialsearch

import java.awt.image.BufferedImage
import java.io.{File, IOException}

import javax.imageio.ImageIO

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

  def loadImage(fileName: String): BufferedImage = {
    val file = new File(fileName)
    try {
      ImageIO.read(file)
    }
    catch {
      case e: IOException =>
        println(s"Failed to load image: ${file.getAbsoluteFile}, ${e.getMessage}")
        null
    }
  }

  def readCsvColumns(filename: String): List[List[String]] = {
    readCsvRows(filename).transpose
  }

  object Timing {
    def nanosToMillis(nanos: Long): Double = nanos / 1e6

    def millionsPerSec(timeMillis: Double, count: Int): Double = count/(timeMillis*1000.0)
    def millionsPerSecStr(timeMillis: Double, count: Int): String = {
      f"${millionsPerSec(timeMillis, count)}%.4f (millions/s)"
    }

    // This is simple and probably flawed way to micro benchmark anything but gives consistent results with Google Caliper
    // (may as well be flawed too).
    // TODO: consider rewriting the benchmarks using http://openjdk.java.net/projects/code-tools/jmh/
    def measureTime[T](block: => T, count: Int = 1): Double = {
      val t0 = System.nanoTime()
      for (_ <- 0 until count) {
        val dummy: T = block
        BlackHole.consumeAny(dummy)
      }
      nanosToMillis(System.nanoTime() - t0)
    }


    case class TimingResult(
      name1: String,
      millis1: Double,
      name2: String,
      millis2: Double,
    ){
      def ratio = millis1/millis2

      def toInfo(totalOperationCount: Int): String = {
        s"$name1: ${millionsPerSecStr(millis1, totalOperationCount)}\n" +
        s"$name2: ${millionsPerSecStr(millis2, totalOperationCount)}\n" +
        s"Ratio: ($name1/$name2): $ratio"
      }
    }

    def compareTime[T](name1: String, block1: => T, name2: String, block2: => T, count: Int = 1): TimingResult = {
      var t1 = 0.0
      var t2 = 0.0
      for(_ <- 0 until count){
        t1 += measureTime(block1)
        t2 += measureTime(block2)
      }

      TimingResult(name1, t1, name2, t2)
    }

    def measureTimeWithInit[T, U](initBlock: => T, block: => U, count: Int = 1): Double ={
      var tSum = 0L
      for(_ <- 0 until count){
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
      for (_ <- 0 until warmUpCount) BlackHole.consumeDouble(measureTimeWithInit(initBlock, block, count))
      measureTimeWithInit(initBlock, block, count)
    }
    
    def warmUpAndMeasureTime(block: => Any, count: Int, warmUpCount: Int = defaultWarmupCount): Double = {
      for (_ <- 0 until warmUpCount) BlackHole.consumeDouble(measureTime(block, count))
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
