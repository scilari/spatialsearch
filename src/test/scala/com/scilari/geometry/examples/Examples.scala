package com.scilari.geometry.examples

import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.trees.multitree.MultiTree
import com.scilari.geometry.spatialsearch.trees.quadtree.QuadTree

// scalastyle:off
object Examples {

  val points = Seq.fill(100)(Float2.random)
  val tree = QuadTree(points)
  val tree3 = QuadTree(points.map(_ + Float2(1.0, 1.0)))
  val queryPoint = Float2(0.5, 0.5)

  object Insertion{

    import com.scilari.geometry.models.{Float2, DataPoint}
    import com.scilari.geometry.spatialsearch.SearchTree

    class MyData() // dummy data class

    // create some random points
    val points = Seq.fill(100)(Float2.random)

    // wrap your data with coordinates
    val dataPoints = points.map{p => DataPoint(p, new MyData())}
    val tree1 = SearchTree(dataPoints)

    import com.scilari.geometry.models.AABB

    val tree2 = SearchTree[DataPoint[MyData]](AABB.unit)
    tree2.add(dataPoints)

    val tree3 = SearchTree(points)
    val outsidePoints = Seq.fill(100)(Float2(1, 1) + Float2.random)
    outsidePoints.foreach(tree3.addEnclose)

  }

  object BasicQueries{
    val queryPoint = Float2(0.5, 0.5)
    val knn = tree.knnSearch(queryPoint, k = 10)
    val nn = tree.nearestNeighborSearch(queryPoint)
    val range = tree.rangeSearch(queryPoint, r = 0.2f)
    val poly = tree.polygonalSearch(queryPoint)
  }

  object SeqBasedSearches{
    val queryPoints = Array.fill(10)(Float2.random)
    val knn = tree.seqKnnSearch(queryPoints, k = 10)
    val range = tree.seqRangeSearch(queryPoints, r = 0.2f)
  }

  object Removal{
    val toBeRemoved = points.take(20)

    // Using coordinates to traverse straight to the leaf
    points.foreach(p => tree.remove(p))
  }

  object MultiTreeSearch{
    // Combining two different trees
    val multiTree = MultiTree(Seq(tree, tree3))

    // Using for queries as before
    val knn = multiTree.knnSearch(queryPoint, 10)
  }

}
