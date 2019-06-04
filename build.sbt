name := "spark-ml-tuning"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.8"

libraryDependencies ++= {
  val sparkVersion = "2.4.3"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.scalatest" %% "scalatest" % "3.0.1" % Test
  )
}
