name := "AdBlock"

version := "1.0"

scalaVersion := "2.12.1"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.19",
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.8.0",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.26")