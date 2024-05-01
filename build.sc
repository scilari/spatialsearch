import mill._, scalalib._
import mill.scalalib.publish._

object spatialsearch extends ScalaModule {
  def scalaVersion = "3.3.3"

  def publishVersion = "0.0.1"
  def pomSettings = PomSettings(
    description = "Scala Spatial Search library",
    organization = "com.scilari",
    url = "https://github.com/scilari/spatialsearch_dotty",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("scilari", "spatialsearch_dotty"),
    developers = Seq(
      Developer("scilari", "Ilari Vallivaara", "https://github.com/scilari")
    )
  )

  object test extends ScalaTests with TestModule.ScalaTest {
    override def ivyDeps =
      Agg(
        ivy"org.scalatest::scalatest:3.2.10",
        ivy"org.scalacheck::scalacheck:1.15.4"
      )

  }
}
