
import Versions._

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

lazy val root = (project in file(".")).aggregate(play24).settings(
  publishArtifact := false
)

crossScalaVersions := Seq("2.10.6", "2.11.7")

lazy val play24 = (project in file("play-2.4")).settings(
  name := "play-json-service-lib-2.4",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % play24Version % "provided",
    "com.typesafe.play" %% "play-json" % play24Version % "provided",
    "com.typesafe.play" %% "twirl-api" % "1.1.1" % "provided"
  )
)


