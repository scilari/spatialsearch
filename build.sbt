import sbt.{Project, ProjectRef, uri}

lazy val baseSettings = Seq(
  name := "spatialsearch",
  organization := "com.scilari",
  version := "0.3.3-SNAPSHOT",
  scalaVersion := "2.13.3"
)


lazy val root = project.in(sbt.file("."))
  .dependsOn(geolib).aggregate(geolib)
  .settings(libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.0" % Test,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
  ))
  .settings(baseSettings: _*)



lazy val geolib = RootProject(uri("ssh://git@github.com/scilari/geolib.git#master"))

publishMavenStyle := true

crossScalaVersions := Seq("2.11.8", "2.12.8")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
scalacOptions ++= Seq("-target:jvm-1.8"/*, "-optimize"*/)

publishArtifact in Test := false

parallelExecution in Test := false

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>https://github.com/scilari/spatialsearch</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>scilari</id>
        <name>Ilari Vallivaara</name>
        <url>https://scilari.com/</url>
      </developer>
    </developers>
    