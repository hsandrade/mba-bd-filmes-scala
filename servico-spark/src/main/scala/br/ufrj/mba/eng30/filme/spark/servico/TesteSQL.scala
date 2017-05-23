package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark.sql.SparkSession

object TesteSQL {

   def main(args: Array[String]) {

    val spark = SparkSession
      .builder
      .appName("TesteScalaSpark")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._

    spark.close();
  }
   
}