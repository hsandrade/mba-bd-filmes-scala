name := "servico-spark"
version := "1.0"
scalaVersion := "2.11.8"
resolvers += "Job Server Bintray" at "https://dl.bintray.com/spark-jobserver/maven"
libraryDependencies += "spark.jobserver" %% "job-server-api_2.11" % "0.7.0" % "provided"
