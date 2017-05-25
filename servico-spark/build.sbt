name := "servico-spark"
version := "1.0"
scalaVersion := "2.11.8"
resolvers += "Job Server Bintray" at "https://dl.bintray.com/spark-jobserver/maven"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.1.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.1.1"
libraryDependencies += "spark.jobserver" %% "job-server-api" % "0.7.0" % "provided"

