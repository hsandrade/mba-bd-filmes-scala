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
 * Objeto para calcular os Top X Atores Rentaveis.
 */
object TopAtorRentavel extends NewSparkJob {

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
      .appName("TopAtorRentavel")
      .getOrCreate()

    import spark.implicits._

    //criar o schema para parse do CSV
    val structCli = strucTypeCsv()

    //criar um DataFrame a partir de um CSV, indicando que a primeira linha eh o cabecalho (para ignorar).
    //cache para otimizar futuras utilizacoes do fileDf
    val fileDf = spark.read.option("header", "true").schema(structCli).csv(urlCsvFilmes).distinct.cache

    //funcao para montar objeto com par NomeAtor,Valor_Arrecadado_Filme
    def processarCampoAtor(nomeCampo: String, linha: Row) = {
      //obtem como string visto que ha valores null no csv
      //Some eh uma expressao logica, onde permite retornar o valor ou caso seja null (getOrElse), retorna 0 como padrao
      //evitando classCastException
      //O gross teve que ser obtido sem fieldName para evitar classCastException de Long devido a valores null no Row
      val gross = Some(linha.get(8)).getOrElse(0).asInstanceOf[Long]
      (linha.getAs[String](fieldName = nomeCampo), gross)
    }
    
    //cria um map de acordo com o campo de nome do ator e valor de arrecadacao do filme, 
    //a ordem dos campos eh de acordo com o StructType associado ao csv
    val mapAtor1 = fileDf.map(linha => processarCampoAtor("actor_1_name", linha))
    val mapAtor2 = fileDf.map(linha => processarCampoAtor("actor_2_name", linha))
    val mapAtor3 = fileDf.map(linha => processarCampoAtor("actor_3_name", linha))

    //realiza a juncao entre os maps para realizar as demais acoes
    val mapAtorUnion = mapAtor1.union(mapAtor2).union(mapAtor3)
    
    //realiza a soma e contagem de filmes agrupando por nome do ator ("_1"),
    //apos operacoes com map, nao foi possivel manter o nome original das colunas de acordo com o StructType
    //TODO verificar como manter o nome original das colunas
    val mapSomaAtor = mapAtorUnion.groupBy("_1").agg(sum("_2").as("gross"), count("_1").as("qtd_movies"))
    
    //criar tabela temporaria a partir do resultado do CSV para executar as consultas
    mapSomaAtor.createOrReplaceTempView("topAtorTemp")

    //executa consulta 
    val queryDf = spark.sql("select _1 as actor_name, gross, qtd_movies from topAtorTemp order by gross desc limit " + data)

    //retorna um String representando o JSON do DataFrame
    queryDf.toJSON.collect
  }

  /**
   * Valida se a aplicacao recebeu algum parametro de acordo com o job a ser processado.
   */
  def validate(sc: SparkContext, runtime: JobEnvironment, config: Config): JobData Or Every[ValidationProblem] = {
    Try(config.getString("top"))
      .map(words => Good(words))
      .getOrElse(Bad(One(SingleProblem("No input.string param"))))
  }

}