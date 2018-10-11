# Incremental Spatial Search

<p>
  <img align="right" src="images/quadtree.png" width="350"/>
</p>

Scala implementation of the incremental spatial search implementation described
e.g. in Samet: Foundations of Multidimensional and Metric Data Structures.
* The main purpose is two-fold:
  * Offer a performant and very feature-rich default search tree implementation
  * Offer a very general framework to build future features on: the incremental spatial search allows very flexible queries where
e.g. the search state is manipulated on the fly (e.g. filter elements and prune nodes in the queues)

* In addition to basic queries (range, knn, knnWithCondition), also a sequence-based query is supported.
This finds the closest points (or points inside a range) for a sequence of query points.
Useful for finding points along a path, for example.

* MultiTree implementation takes in multiple search trees to offer convenient queries from
the combined hierarchies. This can be utilized e.g. in 
[ancestry tree-based SLAM](https://www.tandfonline.com/doi/abs/10.1080/01691864.2018.1436468?journalCode=tadr20)
or whenever different sets of objects are stored in different trees.
This is implemented by initializing the search state with the tree roots.

* Currently only quadtree is implemented as a concrete and well-optimized class (QuadTree)

* R-tree implemented only as a proof-of-concept (RTree)

* Work in progress


## Usage
### Insertion
#### If the points are known
``` scala
import com.scilari.geometry.models.{Float2, DataPoint}
import com.scilari.geometry.spatialsearch.SearchTree

class MyData() // dummy data class

// create some random points
val points = Seq.fill(100)(Float2.random)

// wrap your data with coordinates
val dataPoints = points.map{p => DataPoint(p, new MyData())}
val tree1 = SearchTree[DataPoint[MyData]](dataPoints)
```
#### If the area is known
``` scala
import com.scilari.geometry.models.AABB

val tree2 = SearchTree[DataPoint[MyData]](AABB.unit)
tree2.add(dataPoints)
```

#### If the area must expand based on incoming data
``` scala
val tree3 = SearchTree(points)
val outsidePoints = Seq.fill(100)(Float2(1, 1) + Float2.random)
outsidePoints.foreach(tree3.addEnclose)
```

### Basic Queries
``` scala
val queryPoint = Float2(0.5, 0.5)
val knn = tree.knnSearch(queryPoint, 10)
val range = tree.rangeSearch(queryPoint, 0.2f)
val poly = tree.polygonalSearch(queryPoint)
```

### Queries using sequences of points
Useful for finding the elements that are closest to a path (defined as points), for example.
Buffer used for performance (thread-unsafe sentinel tricks).
``` scala
val queryPoints = mutable.Buffer.fill(10)(Float2.random)
val knn = tree.seqKnnSearch(queryPoints, k = 10)
val range = tree.seqRangeSearch(queryPoints, r = 0.2f)
```


### Point removal
``` scala
val toBeRemoved = points.take(20)

// Using coordinates to traverse straight to the leaf
points.foreach(p => quadTree.remove(p))
```

### MultiTree functionality
``` scala
// Combining two different trees
val multiTree = MultiTree(Seq(tree, tree3))

// Using for queries as before
val knn = multiTree.knnSearch(queryPoint, 10)
```

For more detailed examples, see test cases.

## Performance
Naive performance tests run against a 
[Java kd-tree implementation](http://robowiki.net/wiki/User:Chase-san/Kd-Tree) with 10k inserted points
show that the QuadTree performance is about 20-35% better in insertions, and knn and range searches.

```
== Test info == 
Point count: 10000
Run count: 200
Query count (per run): 1000
Insert run count: 2000
Knn k: 100
Range: 250.0 out of total point area of 1000.0 x 1000.0
Quadtree. depth: 7 nodeCount: 633
================

== Insert time == 
KDTree: 1.697278063E-4 (ms/insert)
QuadTree: 1.2778786165E-4 (ms/insert)
Ratio (Quad/KD): 0.7528987997649033

== Knn query time == 
KDTree: 0.023937707545 (ms/query)
QuadTree: 0.01544552218 (ms/query)
Ratio (Quad/KD): 0.645238152022882

== Range query time ==
KDTree: 0.044225099440000006 (ms/query)
QuadTree: 0.028976486620000002 (ms/query)
Ratio (Quad/KD): 0.6552045554880498
...
```

## TODO:
* Support for Manhattan and rectangular range queries
* Improve this document (usage, visualization etc.)
* Optimize r-tree (performance is not very good ATM)
* Using as a dependency
