// package com.scilari.geometry.performance

// import com.csdgn.util.KDTree
// import com.scilari.geometry.models.Float2
// import com.scilari.geometry.performance.PerformanceBase.{queryArray, queryPoints}
// import com.scilari.geometry.spatialsearch.quadtree.{Parameters, QuadTree}
// import com.scilari.math.ArrayUtils.linSpace
// import jk.tree.KDTree.Euclidean
// import org.scalameter.{Key, KeyValue}
// import org.scalameter.api._
// import org.scalameter.picklers.Implicits._
// import org.scalameter.Measurer._
// import org.scalameter.picklers.Pickler
// import org.scalameter.picklers.noPickler._

// object QuadTreeBenchmark extends Bench.OfflineReport  {
  
//   val baseDir = "target/benchmarks/report/"

//   // val dirKeyValue = (reports.resultDir -> (baseDir + "knn")).asInstanceOf[KeyValue]
//   val runKeyValue = (exec.benchRuns -> 200).asInstanceOf[KeyValue]
  

//   override def executor = LocalExecutor(
//     new Executor.Warmer.Default,
//     Aggregator.average,
//     new Measurer.Default
//   )
  
//   case class DataAndTrees(
//     points: collection.Seq[Float2],
//     pointsArray: collection.Seq[Array[Double]],
//     quadTree: QuadTree[Float2],
//     quadTreeCompressed: QuadTree[Float2],
//     skilgannonKdTree: Euclidean[Float2],
//     chaseKdTree: KDTree[Float2]
//   )

//   def createDataAndTrees(pointCount: Int): DataAndTrees = {
//     val points = PerformanceBase.createPoints(pointCount)
//     val pointsArray: collection.Seq[Array[Double]] = points.map {
//       _.position.toDoubleArray
//     }

//     val quadTree = QuadTree(points)

//     val quadTreeCompressed = QuadTree(points)
//     quadTreeCompressed.root.compress()

//     val skilgannonKdTree = new Euclidean[Float2](2)
//     pointsArray.foreach(p => skilgannonKdTree.addPoint(p, Float2.random))

//     val chaseKdTree = new KDTree[Float2](2)
//     pointsArray.foreach(k => chaseKdTree.add(k, Float2.random))

//     DataAndTrees(points, pointsArray, quadTree, quadTreeCompressed, skilgannonKdTree, chaseKdTree)

//   }

//   //val pointCounts = Seq(100, 500, 1000, 5000, 20000)
//   val pointCounts = Seq(100, 500, 1000, 5000)


//   // Run the actual benchmarks
  
//   pointCounts.foreach(knnBenchmark(_))
//   // pointCounts.foreach(rangeBenchmark(_))
//   // insertBenchmark
 

//   def knnBenchmark(pointCount: Int): Unit = {
//     val dataAndTrees = createDataAndTrees(pointCount)
//     import dataAndTrees._

//     val knnCounts = Gen.enumeration("k")(
//       Seq(1, 2, 5, 10, 25, 50, 100, 250, 500).filter(_ <= pointCount/2): _*)

    
//     performance of s"Knn Query using $pointCount points" config(runKeyValue)  in {
//       using(knnCounts) curve "QuadTree knn" in { k => queryPoints.foreach { quadTree.knnSearch(_, k)}}
//       using(knnCounts) curve "QuadTree (compr.) knn" in { k => queryPoints.foreach{ quadTreeCompressed.knnSearch(_, k)}}
//       using(knnCounts) curve "Skilgannon kd-tree knn" in { k => queryArray.foreach { skilgannonKdTree.nearestNeighbours(_, k)}}
//       using(knnCounts) curve "Chase kd-tree knn" in { k => queryArray.foreach { chaseKdTree.getNearestNeighbors(_, k)}}
//     }
//   }

//   def rangeBenchmark(pointCount: Int): Unit ={
//     val dataAndTrees = createDataAndTrees(pointCount)
//     import dataAndTrees._
    
//     val ranges = Gen.enumeration[Float]("radius (ratio)")(linSpace(0.02f, 0.2f, 10): _*)
      
//     val width = PerformanceBase.bb.width
    
//     performance of s"Range query using $pointCount points" config(runKeyValue) in {
      
//       using(ranges) curve "QuadTree range" in { r => 
//         queryPoints.foreach { quadTree.rangeSearch(_, r * width)}}
      
//       using(ranges) curve "QuadTree (compr.) range" in { r => 
//         queryPoints.foreach { quadTreeCompressed.rangeSearch(_, r * width)}}
      
//       using(ranges) curve "Skilgannon kd-tree range" in { r => 
//         queryArray.foreach { skilgannonKdTree.ballSearch(_, r * width)}}
//     }
//   }
  
//   def insertBenchmark: Unit ={
//     case class PointData(
//       points: collection.Seq[Float2], 
//       arrays: collection.Seq[Array[Double]]
//     )
    
//     val insertPointCounts = Seq(50, 100, 250, 500, 750, 1000, 2000, 5000, 10000, 20000)
    
//     val countRanges = Gen.enumeration("point count")(insertPointCounts: _*)
//     val ranges: Gen[PointData] = for( count <- countRanges) yield {
//       val ps = PerformanceBase.createPoints(count)
//       PointData(ps, ps.map {_.position.toDoubleArray})
//     }
    
//     performance of s"Insertion of points" config((exec.benchRuns -> 1000).asInstanceOf[KeyValue]) in {
      
//       using(ranges) curve "Quadtree insert" in { pointData =>
//         val p = Parameters(100)
//         val tree = QuadTree[Float2](PerformanceBase.bb, p)
//         tree.add(pointData.points)
//       }

//       using(ranges) curve "Skilgannon kd-tree insert" in { pointData =>
//         val skilgannonKdTree = new Euclidean[Float2](2)
//         pointData.arrays.foreach(p => skilgannonKdTree.addPoint(p, Float2.zero))
//       }
      
//       using(ranges) curve ("Chase kd-tree insert") in { pointData =>
//         val chaseKdTree = new KDTree[Float2](2)
//         pointData.arrays.foreach(k => chaseKdTree.add(k, Float2.zero))
//       }

//     }
    
    
    
//   }

// }
