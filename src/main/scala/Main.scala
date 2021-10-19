import com.scilari.geometry.models.Float2
import com.scilari.geometry.spatialsearch.quadtree.QuadTree


object Main {
  def main(args: Array[String]): Unit = {
    val p: Float2 = Float2(2, 2)
    
    val ps = (0 until 100).map{_ => Float2.randomMinusOneToOne}
    
    val tree = QuadTree(ps)
    
    println("nodes: " + tree.root.nodes)
    
    val elems = tree.knnSearch(Float2.zero, 10)
    println(elems)
    
    println(s"Hello world! $p")
    println(msg)
  }

  def msg = "I was compiled by dotty :)"

}
