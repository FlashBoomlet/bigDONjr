import sbt.Keys.javaOptions

// scalastyle:off

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

mainClass in (Compile, run) := Some("com.flashboomlet.WordStormDriver")

lazy val root =
  (project in file(".")).aggregate(
    wordStorm
  )

lazy val commonSettings = Seq(
  organization := "com.flashboomlet",
  scalaVersion := "2.11.8",
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Typesafe Releases" at "https://repo.typesafe.com/typesafe/maven-releases/",
    "Maven central" at "http://repo1.maven.org/maven2/"
  ),
  libraryDependencies ++= Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.11.13",
    "com.typesafe.akka" % "akka-actor_2.11" % "2.4.7",
    "com.flashboomlet" %% "datascavenger" % "0.1.0",
    "ch.qos.logback"  %  "logback-classic" % "1.1.3",
    "org.slf4j" %  "slf4j-api" % "1.7.14",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"))

lazy val wordStorm = (project in file ("wordStorm"))
  .settings(commonSettings: _*)
  .settings(
    name := "wordStorm",
    version := "0.0.0",
    javaOptions += "-Dlogback.configurationFile=../dataScavenger/src/main/resources/logback.xml")
