# Incremental Spatial Search


<style="float: right;" img src="images/quadtree.png" width="350"/>



Scala implementation of the incremental spatial search implementation described
e.g. in Samet: Foundations of Multidimensional and Metric Data Structures.
* The purpose is to offer very general framework
to build on: the incremental spatial search allows very flexible queries where
e.g. the search state is manipulated on the fly (e.g. filter elements and prune nodes in the queues)
* Currently only quadtree is implemented as a concrete and well-optimized class (QuadTree)
* R-tree implemented as a proof-of-concept (RTree)
* Work in progress

## Usage
### Insertion
#### If the points are known
``` scala
class MyData()
val points = Seq.fill(100)(Float2.random)
// wrap your data with coordinates
val dataPoints = points.map{p => DataPoint(p, new MyData())}
val quadTree = QuadTree[DataPoint[MyData]](points)
```
#### If the area is known
``` scala
val quadTree = QuadTree[Float2](AABB.unit)
points.foreach(quadTree.add)
```

#### If the area must expand based on incoming data
``` scala
val quadTree = QuadTree(points)
val outsidePoints = Seq.fill(100)(Float2(1, 1) + Float2.random)
outsidePoints.foreach(quadTree.addEnclose)

```

### Queries
``` scala
val queryPoint = Float2(0.5, 0.5)
val knn = quadTree.knnSearch(queryPoint, 10)
val range = quadTree.rangeSearch(queryPoint, 0.2)
val poly = quadTree.polygonalSearch(queryPoint)

```

### Point removal
``` scala
val toBeRemoved = points.take(20)

// Using coordinates to traverse straight to the leaf
points.foreach(p => quadTree.remove(p))

```

For more detailed examples, see test cases.

## TODO:
* Improve this document (usage, visualization etc.)
* Describe how to define your own searches
* Optimize r-tree (performance is not very good ATM)
