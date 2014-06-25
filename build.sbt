
import Versions._

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

lazy val root = (project in file(".")).aggregate(play22, play23).settings(
  publishArtifact := false
)

crossScalaVersions := Seq("2.10.4", "2.11.1")

// We jump through some hoops to avoid compiling and publishing a scala 2.11
// version of the play-2.2 version of this library, since play-2.2 doesn't
// support scala 2.11. Feels like there should be an easier way to do this;
// override crossScalaVersions inside the project doesn't work.
lazy val play22: Project = (project in file("play-2.2")).settings(
  name := "play-json-service-lib-2.2",
  scalaVersion := "2.10.4",
  libraryDependencies <++= scalaBinaryVersion {
    case "2.10" => Seq(
      "com.typesafe.play" %% "play" % play22Version % "provided",
      "com.typesafe.play" %% "play-json" % play22Version % "provided"
    )
    case "2.11" => Nil
  },
  publishArtifact := scalaBinaryVersion.value == "2.10",
  (unmanagedSourceDirectories in Compile) += baseDirectory.value / "src" / "main" / s"scala-${scalaBinaryVersion.value}"
)

lazy val play23 = (project in file("play-2.3")).settings(
  name := "play-json-service-lib-2.3",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % play23Version % "provided",
    "com.typesafe.play" %% "play-json" % play23Version % "provided",
    "com.typesafe.play" %% "twirl-api" % "1.0.2" % "provided"
  )
)


