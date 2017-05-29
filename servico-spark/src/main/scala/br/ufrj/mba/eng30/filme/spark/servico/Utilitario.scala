package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark.sql._
import org.apache.spark.sql.types._

/**
 * Classe utilitaria contendo definicoes comuns para demais objetos.
 */
object Utilitario {

  val urlBanco = "jdbc:mysql://172.31.16.61:3306/spark_result"
  
  //definir propriedades de acesso ao banco MySql
  val propBanco = new java.util.Properties
  propBanco.setProperty("user", "sparkjob")
  propBanco.setProperty("password", "ufrjmbajob")  

  /**
   * Definicao do StructType referente ao CSV de filmes
   */
  def strucTypeCsv(): StructType = {
    StructType(
      StructField("color", StringType) ::
        StructField("director_name", StringType) ::
        StructField("num_critic_for_reviews", LongType) ::
        StructField("duration", IntegerType) ::
        StructField("director_facebook_likes", LongType) ::
        StructField("actor_3_facebook_likes", LongType) ::
        StructField("actor_2_name", StringType) ::
        StructField("actor_1_facebook_likes", LongType) ::
        StructField("gross", LongType) ::
        StructField("genres", StringType) ::
        StructField("actor_1_name", StringType) ::
        StructField("movie_title", StringType) ::
        StructField("num_voted_users", LongType) ::
        StructField("cast_total_facebook_likes", LongType) ::
        StructField("actor_3_name", StringType) ::
        StructField("facenumber_in_poster", IntegerType) ::
        StructField("plot_keywords", StringType) ::
        StructField("movie_imdb_link", StringType) ::
        StructField("num_user_for_reviews", LongType) ::
        StructField("language", StringType) ::
        StructField("country", StringType) ::
        StructField("content_rating", StringType) ::
        StructField("budget", LongType) ::
        StructField("title_year", IntegerType) ::
        StructField("actor_2_facebook_likes", LongType) ::
        StructField("imdb_score", FloatType) ::
        StructField("aspect_ratio", FloatType) ::
        StructField("movie_facebook_likes", StringType) :: Nil)
  }

  /**
   * URL do Hadoop onde o CSV de filmes se encontra
   */
  def urlCsvFilmes: String = {
    "hdfs://hadoop-master:9000/mba/dados/movie_metadata.csv"
  }

}