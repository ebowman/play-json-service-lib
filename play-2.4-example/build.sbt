
scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

lazy val root =
  (project in file(".")).enablePlugins(PlayScala, SbtTwirl).settings(
    name := "play-2.4-example",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "ie.boboco" %% "play-json-service-lib-2-4" % "1.2.0"
    ),
    TwirlKeys.templateFormats += ("json" -> "com.gilt.play.json.templates.JsonFormat")
  )

