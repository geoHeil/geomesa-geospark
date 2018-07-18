package com.github.geoheil.geomesaGeospark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.geosparksql.UDT.UdtRegistratorWrapper
import org.apache.spark.sql.geosparksql.strategy.join.JoinQueryDetector
import org.datasyslab.geosparksql.UDF.Catalog

object CustomGeosparkRegistrator {

  def registerAll(sparkSession: SparkSession): Unit = {
    sparkSession.experimental.extraStrategies = JoinQueryDetector :: Nil
    UdtRegistratorWrapper.registerAll()
    Catalog.expressions.foreach(f => sparkSession.sessionState.functionRegistry.registerFunction("geospark_" + f.getClass.getSimpleName.dropRight(1), f))
    Catalog.aggregateExpressions.foreach(f => sparkSession.udf.register("geospark_" + f.getClass.getSimpleName, f))
  }

}
