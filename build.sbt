scalaVersion := "2.12.8"

organization := "com.github.iwag"

name := "sample"

val finagleVersion = "19.5.0"

libraryDependencies ++= Seq(
  "com.twitter"    %% "twitter-server"        % finagleVersion,
  "com.twitter" %% "twitter-server-slf4j-jdk14" % finagleVersion,
  "com.twitter"    %% "finagle-http"         % finagleVersion,
  "org.scalatest"  %%  "scalatest"        % "3.0.0" % "test"
)


resolvers += "twitter" at "http://maven.twttr.com"
