package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark._
import org.apache.spark.SparkContext._
import spark.jobserver.api.{ SparkJob => NewSparkJob, _ }
import com.typesafe.config.Config
import org.scalactic._
import scala.util.Try
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import Utilitario._

/**
 * Objeto para gerar uma lista normalizada de generos encontrados no CSV.
 */
object NormalizaGenero extends NewSparkJob {

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
    //inicia uma session com suporte a SQL
    val spark = SparkSession
      .builder()
      .appName("NormalizaGenero")
      //.enableHiveSupport()
      .getOrCreate()
      
     import spark.implicits._

    //criar o schema para parse do CSV
    val structCli = strucTypeCsv()

    //criar um DataFrame a partir de um CSV, indicando que a primeira linha eh o cabecalho (para ignorar).
    val fileDf = spark.read.option("header", "true").schema(structCli).csv(urlCsvFilmes)
    
    //gera um map de generos a partir de cada filme
    def listaGeneros(linha: Row ) = {
      linha.getAs[String]("genres").split("\\|")
    }
    
    //prepara um flatMap para representar cada item do conjunto de array como sendo individual,
    //evitando combinacao de genero devido a estrutura do CSV
    //toDF("genre") define o nome da primeira coluna gerada apos o map, evitando nome automatico
    val mapTemp = fileDf.flatMap(linha => listaGeneros(linha)).toDF("genre").cache

    mapTemp.createOrReplaceTempView("generos")
    
    //retorna um String representando o JSON do DataFrame
    spark.sql("select distinct genre from generos order by genre asc").toJSON.collect
  }

  /**
   * Valida se a aplicacao recebeu algum parametro de acordo com o job a ser processado.
   */
  def validate(sc: SparkContext, runtime: JobEnvironment, config: Config): JobData Or Every[ValidationProblem] = {
    Good(config.getString(""))
  }

}