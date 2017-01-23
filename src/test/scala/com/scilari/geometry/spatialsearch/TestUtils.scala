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

  def readCsvColumns(filename: String) = {
    readCsvRows(filename).transpose
  }

}
