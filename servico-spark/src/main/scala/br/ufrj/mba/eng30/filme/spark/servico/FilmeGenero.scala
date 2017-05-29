package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark._
import org.apache.spark.SparkContext._
import spark.jobserver.api.{ SparkJob => NewSparkJob, _ }
import com.typesafe.config.Config
import org.scalactic._
import scala.util.Try
import org.apache.spark.sql._
import org.apache.spark.sql.types._

import Utilitario._

/**
 * Lista os filmes por Genero.
 */
object FilmeGenero extends NewSparkJob {

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
      .appName("FilmeCategoria")
      //.enableHiveSupport()
      .getOrCreate()

    //criar o schema para parse do CSV
    val structCli = strucTypeCsv()

    //criar um DataFrame a partir de um CSV, indicando que a primeira linha eh o cabecalho (para ignorar).
    val fileDf = spark.read.option("header", "true").schema(structCli).csv(urlCsvFilmes).distinct

    //criar tabela temporaria a partir do resultado do CSV para executar as consultas
    fileDf.createOrReplaceTempView("filmeCategTemp")
    
    //executa consulta 
    val queryDf = spark.sql("select distinct movie_title, title_year, gross, genres, movie_imdb_link, imdb_score " 
        + "from filmeCategTemp "
        + " where instr(genres,\"" + data + "\") > 0"
        + " order by movie_title asc limit 50")

    //retorna um String representando o JSON do DataFrame
    queryDf.toJSON.collect
  }

  /**
   * Valida se a aplicacao recebeu algum parametro de acordo com o job a ser processado.
   */
  def validate(sc: SparkContext, runtime: JobEnvironment, config: Config): JobData Or Every[ValidationProblem] = {
    Try(config.getString("genero"))
      .map(words => Good(words))
      .getOrElse(Bad(One(SingleProblem("No input.string param"))))
  }

}