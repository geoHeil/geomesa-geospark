package com.github.geoheil.geomesaGeospark

import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.FunctionRegistry
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DecimalType
import org.datasyslab.geosparksql.utils.GeoSparkSQLRegistrator
import org.locationtech.geomesa.spark.jts._


case class MyPoint(id: Int, x: Double, y: Double)

case class MyGeometry(id: Int, wkt: String)

object Foo extends App {

  val spark = SparkSession
    .builder()
    .config(new SparkConf()
      .setAppName("geomesaGeospark")
      .setMaster("local[*]")
      .setIfMissing("spark.serializer",
        classOf[KryoSerializer].getCanonicalName)
      .setIfMissing("spark.kryo.unsafe", "true")
      .setIfMissing("spark.kryo.referenceTracking", "false")
      .setIfMissing("spark.kryo.registrator",
        classOf[SpatialKryoRegistrator].getName)
      .setIfMissing("spark.kryo.registrationRequired", "true")
      .setIfMissing("geospark.join.gridtype", "kdbtree"))
    .enableHiveSupport()
    .getOrCreate()

  import spark.implicits._

  // register spatial functions
  // now using custom namespace
  CustomGeosparkRegistrator.registerAll(spark)
  // register geomesa functions
  spark.withJTS
  // register geospark functions TODO WARNING function names overlap. Require custom registrator
  //GeoSparkSQLRegistrator.registerAll(spark)

  val points = Seq(MyPoint(1, 30, 10), MyPoint(2, 31, 35)).toDS
    .withColumn("x", col("x").cast(DecimalType(38, 18)))
    .withColumn("y", col("y").cast(DecimalType(38, 18)))
  points.show
  val polygons = Seq(MyGeometry(1, "POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),\n(20 30, 35 35, 30 20, 20 30))"), MyGeometry(2, "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),(30 20, 20 15, 20 25, 30 20)))")).toDS
  polygons.show

  // perform some sptial computations in geomesa (these functions are not available in geospark
  val polygonsEnriched = polygons
    .withColumn("centeroid_x", st_x(st_centroid(st_geomFromWKT(col("wkt")))))
    .withColumn("centeroid_y", st_y(st_centroid(st_geomFromWKT(col("wkt")))))

  polygonsEnriched.show

  // perform a spatial join, first create spatial binary geometry types using geospark functions, then join
  val pointsGeom = points.withColumn("geom_points", expr(s"geospark_ST_Point(x, y)"))
  pointsGeom.show
  val polygonsGeom = polygonsEnriched.withColumn("geom_polygons", expr(s"geospark_ST_GeomFromWKT(wkt)"))
  polygonsGeom.show


  // TODO 1) how can I get rid of the textual SQL below and actually use DSL expressions? Or at least expr()? Seems only to work on map side operations as above
  pointsGeom.createOrReplaceTempView("points")
  polygonsGeom.createOrReplaceTempView("polygons")
  val joinedDF =
    spark.sql(
      """
        |SELECT *
        |FROM polygons, points
        |WHERE geospark_ST_Contains(polygons.geom_polygons, points.geom_points)
      """.stripMargin)

  joinedDF.show(false)

  println(joinedDF.count)

}
