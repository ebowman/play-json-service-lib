name := "play-2.2-example"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.gilt" %% "play-json-service-lib-2-2" % "1.0.1"
)

play.Project.playScalaSettings

templatesTypes += ("json" -> "com.gilt.play.json.templates.JsonFormat")
