package com.github.geoheil.geomesaGeospark

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.apache.spark.sql.catalyst.expressions.UnsafeRow

class CommonKryoRegistrator extends KryoRegistrator {

  // inspiration how to register spark internal classes
  // https://github.com/bigdatagenomics/adam/blob/master/adam-core/src/main/scala/org/bdgenomics/adam/serialization/ADAMKryoRegistrator.scala
  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(
      Class.forName(
        "org.apache.spark.sql.execution.joins.UnsafeHashedRelation"))
    kryo.register(Class.forName("[[B"))

    // java classes
    kryo.register(classOf[scala.Array[java.lang.Object]])

    // spark internal classes
    kryo.register(classOf[scala.collection.mutable.WrappedArray.ofRef[_]])
    kryo.register(classOf[org.apache.spark.sql.catalyst.InternalRow])
    kryo.register(
      classOf[org.apache.spark.sql.catalyst.expressions.GenericInternalRow])
    kryo.register(classOf[Array[org.apache.spark.sql.catalyst.InternalRow]])
    kryo.register(classOf[UnsafeRow])
    try {
      kryo.register(
        Class.forName(
          "org.apache.spark.internal.io.FileCommitProtocol$TaskCommitMessage"))
      kryo.register(Class.forName(
        "org.apache.spark.sql.execution.datasources.FileFormatWriter$WriteTaskResult"))
      kryo.register(
        Class.forName(
          "org.apache.spark.sql.execution.datasources.BasicWriteTaskStats"))
      kryo.register(
        Class.forName(
          "org.apache.spark.sql.execution.datasources.ExecutedWriteSummary"))
    } catch {
      case cnfe: java.lang.ClassNotFoundException => {
        println(
          "Did not find Spark internal class. This is expected for earlier Spark versions.")
      }
    }

    // scala specific classes:
    kryo.register(Class.forName("scala.collection.immutable.Set$EmptySet$"))

    // return unit to not get a warning
    ()
  }

}
