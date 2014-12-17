scalaVersion := "2.11.4"

organization := "com.github.iwag"

name := "sample"

libraryDependencies ++= Seq(
  "com.twitter"    %% "twitter-server"        % "1.9.0",
  "com.twitter"    %% "finagle-httpx"         % "6.24.0",
  "org.scalatest"  %  "scalatest_2.10"        % "2.2.1" % "test"
)


resolvers += "twitter" at "http://maven.twttr.com"
