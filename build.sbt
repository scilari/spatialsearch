val dottyVersion = "0.27.0-RC1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "spatialsearch-dotty",
    version := "0.1.0",

    scalaVersion := dottyVersion,

    resolvers += "Sonatype OSS Releases" at
      "https://oss.sonatype.org/content/repositories/releases",

    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    
    libraryDependencies += ("com.storm-enroute" % "scalameter_2.13" % "0.19")
      .withDottyCompat("2.13"),

    libraryDependencies += ("org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % "test")
      .withDottyCompat("2.13"),
    
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
    ),

    parallelExecution in Test := false
    
  )
