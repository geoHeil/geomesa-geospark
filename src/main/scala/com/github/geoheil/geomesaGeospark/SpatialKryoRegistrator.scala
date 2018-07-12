package com.github.geoheil.geomesaGeospark

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.locationtech.geomesa.spark.GeoMesaSparkKryoRegistrator

class SpatialKryoRegistrator extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    val commonReg = new CommonKryoRegistrator
    val geomesaReg = new GeoMesaSparkKryoRegistrator
    val geosparkKryoReg = new GeoSparkKryoRegistrator

    commonReg.registerClasses(kryo)
    geomesaReg.registerClasses(kryo)
    geosparkKryoReg.registerClasses(kryo)

    // further required geo-spatial classes
    kryo.register(
      classOf[
        scala.Array[org.datasyslab.geospark.spatialRddTool.StatCalculator]])
    kryo.register(
      classOf[org.datasyslab.geospark.spatialRddTool.StatCalculator])
    kryo.register(Class.forName("[[B"))

    ()
  }
}
