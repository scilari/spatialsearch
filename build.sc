// This is for VSCode/Metals cupport => After mill, also run this: mill mill.contrib.Bloop/install
// import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import mill._, scalalib._

trait BaseModule extends ScalaModule {
  def scalaVersion = "3.3.3"
}

object math extends BaseModule

object geometry extends BaseModule {
  def moduleDeps = Seq(math)
}

object spatialsearch extends BaseModule {
  def moduleDeps = Seq(geometry)

  object test extends ScalaTests with TestModule.ScalaTest {
    override def ivyDeps =
      Agg(
        ivy"org.scalatest::scalatest:3.2.10",
        ivy"org.scalacheck::scalacheck:1.15.4"
      )
  }
}

object examples extends BaseModule {
  def moduleDeps = Seq(spatialsearch)

  override def ivyDeps = Agg(
    ivy"org.creativescala::doodle:0.22.0"
  )

}
