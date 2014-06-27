
scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

lazy val root =
  (project in file(".")).enablePlugins(PlayScala, SbtTwirl).settings(
    name := "play-2.3-example",
    scalaVersion := "2.11.1",
    libraryDependencies ++= Seq(
      "com.gilt" %% "play-json-service-lib-2-3" % "1.0.1"
    ),
    TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")
  )

