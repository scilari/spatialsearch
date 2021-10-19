import mill._, scalalib._

object spatialsearch extends ScalaModule {
  def scalaVersion = "3.1.0-RC3"

  object test extends Tests with TestModule.ScalaTest {
      override def ivyDeps =
      Agg(
        ivy"org.scalatest::scalatest:3.2.10",
        ivy"org.scalacheck::scalacheck:1.15.4"
      )

  }
}