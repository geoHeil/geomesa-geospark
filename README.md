# combine geospatial tools on spark

I need to combine geomesa and geospark on spark, https://github.com/DataSystemsLab/GeoSpark/issues/253.

to execute use:
```
make run
```

fails with
```
ClassCastException: org.apache.spark.sql.catalyst.expressions.UnsafeArrayData cannot be cast to org.apache.spark.sql.catalyst.InternalRow
```

when not using separate registrators. When doing so as suggested in https://github.com/DataSystemsLab/GeoSpark/issues/253

```
Catalog.expressions.foreach(f => FunctionRegistry.builtin.registerFunction("geospark_" + f.getClass.getSimpleName.dropRight(1), f))
Catalog.aggregateExpressions.foreach(f => sparkSession.udf.register("geospark_" + f.getClass.getSimpleName, f))
```

Exeption goes away. But geomesa is used. When renaming functions to `geospark_ST_Point(x, y)` they no longer seem to be defined.

I can't find them in:

```
FunctionRegistry.functionSet.foreach(println)
```