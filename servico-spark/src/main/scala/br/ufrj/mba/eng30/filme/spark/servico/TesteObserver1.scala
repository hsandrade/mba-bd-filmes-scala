package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark._
import org.apache.spark.SparkContext._
import spark.jobserver.api.{ SparkJob => NewSparkJob, _ }
import com.typesafe.config.Config
import org.scalactic._
import scala.util.Try

//https://github.com/spark-jobserver/spark-jobserver/blob/master/job-server-tests/src/main/scala/spark/jobserver/WordCountExample.scala
object TesteObserver1 extends NewSparkJob {

  type JobData = Seq[String]
  //type JobOutput = collection.Map[String, Long]
  type JobOutput = String

  def runJob(sc: SparkContext, runtime: JobEnvironment, data: JobData): JobOutput = {
    //sc.parallelize(data).countByValue
    sc.getConf.get("master")
  }

  def validate(sc: SparkContext, runtime: JobEnvironment, config: Config): JobData Or Every[ValidationProblem] = {
    Try(config.getString("input.string").split(" ").toSeq)
      .map(words => Good(words))
      .getOrElse(Bad(One(SingleProblem("No input.string param"))))
  }

}