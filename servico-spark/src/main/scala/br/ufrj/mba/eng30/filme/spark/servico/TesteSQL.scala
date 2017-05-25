package br.ufrj.mba.eng30.filme.spark.servico

import org.apache.spark.sql._
import org.apache.spark.sql.types._

object TesteSQL {

   def main(args: Array[String]) {

    //https://spark.apache.org/docs/latest/sql-programming-guide.html#hive-tables
    
    val spark = SparkSession
      .builder()
      .appName("TesteHive")
      //.config("spark.sql.warehouse.dir", "hdfs://hadoop-master:9000/mba/dados/hive-warehouse")
      //.config("spark.sql.warehouse.dir", "/opt/mba/spark/tmp-trabalho/tmp-shell/spark-warehouse/")
      .enableHiveSupport()
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._
    import spark.sql
    
    
    //val sqlContext = new org.apache.spark.sql.hive.HiveContext(sc)

    //deletar a tabela para fins de teste
    spark.sql("DROP TABLE IF EXISTS cliente")
    
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
    
    //criar a tabela "permanente" para que seja possível consultá-la via JDBC
    spark.sql("create table cliente as select * from clienteTemp")
    
    //exibir resutlado no console
    spark.sql("select * from cliente").show

    spark.close();
  }
   
}