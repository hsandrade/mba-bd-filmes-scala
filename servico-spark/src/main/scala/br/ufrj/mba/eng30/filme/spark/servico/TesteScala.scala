package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark.sql.SparkSession

object TesteScala {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder
      .appName("TesteScalaSpark")
      .getOrCreate()

    val sc = spark.sparkContext

    val textFile = sc.textFile("hdfs://hadoop-master:9000/mba/teste/wordcount.txt")
    val counts = textFile.flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
    counts.saveAsTextFile("hdfs://hadoop-master:9000/mba/teste/teste-count-1.txt")

    spark.close();
  }

}
