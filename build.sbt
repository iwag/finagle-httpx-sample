scalaVersion := "2.11.8"

organization := "com.github.iwag"

name := "sample"

libraryDependencies ++= Seq(
  "com.twitter"    %% "twitter-server"        % "18.1.0",
  "com.twitter" %% "twitter-server-slf4j-jdk14" % "18.1.0",
  "com.twitter"    %% "finagle-http"         % "18.1.0",
  "org.scalatest"  %  "scalatest_2.10"        % "2.2.1" % "test"
)


resolvers += "twitter" at "http://maven.twttr.com"
