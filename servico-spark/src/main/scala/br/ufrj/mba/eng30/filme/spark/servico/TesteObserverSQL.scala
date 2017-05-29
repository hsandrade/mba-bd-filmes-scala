package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark._
import org.apache.spark.SparkContext._
import spark.jobserver.api.{ SparkJob => NewSparkJob, _ }
import com.typesafe.config.Config
import org.scalactic._
import scala.util.Try
import org.apache.spark.sql._
import org.apache.spark.sql.types._

/**
 * Classe de Teste para utilizar o NewSparkJob,
 * possibilitando que a classe possa ser executada por uma aplicação
 * web remota via Spark JObserver.
 */
object TesteObserverSQL extends NewSparkJob {

  //tipo de objeto a ser informado no parametro da aplicacao
  type JobData = String
  
  //tipo de objeto a ser retornado pelo Job
  type JobOutput = Array[String]

  /**
   * Executa o Job.
   * Recebe sparkContext, 
   * runtime do ambiente do Job,
   * e "data" que representa a informacao enviada como parametro (ex: conteudo do body).
   */
  def runJob(sc: SparkContext, runtime: JobEnvironment, data: JobData): JobOutput = {
    //definir propriedades de acesso ao banco MySql
    val prop = new java.util.Properties
    prop.setProperty("user","sparkjob")
    prop.setProperty("password","ufrjmbajob")
    val urlBanco = "jdbc:mysql://172.31.16.61:3306/spark_result"
    
    //inicia uma session com suporte a SQL
    val spark = SparkSession
      .builder()
      .appName("TesteObserverSQL")
      //.enableHiveSupport()
      .getOrCreate()
      
    //criar o schema para parse do CSV
    val structCli = StructType(
                  StructField("idcli",StringType) :: 
                  StructField("cidade",StringType) ::
                  StructField("sexo",StringType) ::
                  StructField("idade",StringType) ::Nil)      

    //criar um DataFrame a partir de um CSV
    val cliDf = spark.read.schema(structCli).csv("hdfs://hadoop-master:9000/mba/teste/clientes.csv")
    
    //cliDf.show
    
    //criar tabela temporaria a partir do resultado do CSV
    cliDf.createOrReplaceTempView("clienteTemp")

    //executa consulta 
    val cliDfTemp = spark.sql("select idcli,cidade from clienteTemp")
    
    //grava o DataFrame no formato JSON no HDFS (precisa de melhoras, o DataFrame cria um diretorio...)
    //cliDfTemp.toJSON.write.json("hdfs://hadoop-master:9000/mba/teste/clientes-json-2.csv")
    
    //"Resultado 555 - validar : hdfs://hadoop-master:9000/mba/teste/clientes-json-2.csv"
    
    //grava o resultado no banco de dados, recriando a tabela caso ja exista.
    cliDfTemp.write.mode("overwrite").jdbc(urlBanco,"clienteTemp", prop)
    
    //retorna um String representando o JSON do DataFrame
    cliDfTemp.toJSON.collect
  }

  /**
   * Valida se a aplicacao recebeu algum parametro, com base no nome "idCliente".
   */
  def validate(sc: SparkContext, runtime: JobEnvironment, config: Config): JobData Or Every[ValidationProblem] = {
    Try(config.getString("idCliente"))
      .map(words => Good(words))
      .getOrElse(Bad(One(SingleProblem("No input.string param"))))
  }

}