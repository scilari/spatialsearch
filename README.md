# Incremental Spatial Search

Scala implementation of the incremental spatial search implementation described
e.g. in Samet: Foundations of Multidimensional and Metric Data Structures.
* The purpose is to offer very general framework
to build on: the incremental spatial search allows very flexible queries where
e.g. the search state is manipulated on the fly (e.g. filter elements and prune nodes in the queues)
* Currently only quadtree is implemented as a concrete class (QuadTreeEntry)
* Work in progress

## Usage
* For now see test cases

## TODO:
* Improve this document (usage, visualization etc.)
* Implement r-tree functionality
* Refactor
