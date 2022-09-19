// This is for VSCode/Metals cupport => After mill, also run this: mill mill.contrib.Bloop/install
// import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import mill._, scalalib._
import mill.scalajslib._

trait BaseModule extends ScalaModule {
  def scalaVersion = "3.2.0"
}

trait BaseJSModule extends ScalaJSModule with BaseModule {
  def scalaJSVersion = T { "1.10.0" }
  def ivyDeps = Agg(
    ivy"org.scala-js::scalajs-dom::2.1.0",
    ivy"com.lihaoyi::scalatags::0.11.1"
  )
}

object math extends BaseModule

object geometry extends BaseModule {
  def moduleDeps = Seq(math)
}

object spatialsearch extends BaseModule {
  def moduleDeps = Seq(geometry)

  object test extends Tests with TestModule.ScalaTest {
    override def ivyDeps =
      Agg(
        ivy"org.scalatest::scalatest:3.2.10",
        ivy"org.scalacheck::scalacheck:1.15.4"
      )
  }
}

object mathJS extends BaseJSModule {
  def millSourcePath = os.pwd / 'math
}

object geometryJS extends BaseJSModule {
  def millSourcePath = os.pwd / 'geometry
  def moduleDeps = Seq(mathJS)
}

object spatialsearchJS extends BaseJSModule {
  def millSourcePath = os.pwd / 'spatialsearch
  def moduleDeps = Seq(geometryJS)
}

object renderer extends BaseJSModule {
  def moduleDeps = Seq(geometry)
}

object examples extends BaseJSModule {

  object image extends BaseJSModule {
    def moduleDeps = Seq(renderer, spatialsearchJS)
  }
}
