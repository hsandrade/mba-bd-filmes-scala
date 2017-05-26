package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark._
import org.apache.spark.SparkContext._
import spark.jobserver.api.{ SparkJob => NewSparkJob, _ }
import com.typesafe.config.Config
import org.scalactic._
import scala.util.Try
import org.apache.spark.sql._
import org.apache.spark.sql.types._

object TesteObserverSQL extends NewSparkJob {

  type JobData = Seq[String]
  type JobOutput = String

  def runJob(sc: SparkContext, runtime: JobEnvironment, data: JobData): JobOutput = {
    val spark = SparkSession
      .builder()
      .appName("TesteObserverSQL")
      //.enableHiveSupport()
      .getOrCreate()

    //sc.parallelize(data).countByValue
      
    //criar o schema
    val structCli = StructType(
                  StructField("idcli",StringType) :: 
                  StructField("cidade",StringType) ::
                  StructField("sexo",StringType) ::
                  StructField("idade",StringType) ::Nil)      

    //criar um DataFrame a partir de um CSV
    val cliDf = spark.read.schema(structCli).csv("hdfs://hadoop-master:9000/mba/teste/clientes.csv")
    
    cliDf.show
    
    //criar tabela temporaria
    cliDf.createOrReplaceTempView("clienteTemp")

    val cliDfTemp = spark.sql("select * from clienteTemp")
    
    cliDfTemp.toJSON.write.json("hdfs://hadoop-master:9000/mba/teste/clientes-json-2.csv")

    
    "Resultado 333 - validar : hdfs://hadoop-master:9000/mba/teste/clientes-json-2.csv"
    
    //spark.close()
  }

  def validate(sc: SparkContext, runtime: JobEnvironment, config: Config): JobData Or Every[ValidationProblem] = {
    Try(config.getString("input.string").split(" ").toSeq)
      .map(words => Good(words))
      .getOrElse(Bad(One(SingleProblem("No input.string param"))))
  }

}