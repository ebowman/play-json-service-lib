
publishMavenStyle := true

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test in ThisBuild := false

pomIncludeRepository in ThisBuild := { _ => false }

pomExtra in ThisBuild := (
  <scm>
    <url>git@github.com:gilt/play-json-service-lib.git</url>
    <connection>scm:git:git@github.com:gilt/play-json-service-lib.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ebowman</id>
      <name>Eric Bowman</name>
      <url>http://twitter.com/ebowman</url>
    </developer>
  </developers>)