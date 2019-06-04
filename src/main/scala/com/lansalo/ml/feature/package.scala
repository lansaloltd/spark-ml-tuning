package com.lansalo.ml

import org.apache.spark.sql.types._

package object feature {

  // id	gender	age	area	income	education	vote
  val labelledDataSchema = StructType(
    Array(
      StructField("id", LongType, true),
      StructField("gender", StringType, true),
      StructField("age", IntegerType, true),
      StructField("area", StringType, true),
      StructField("income", IntegerType, true),
      StructField("education", IntegerType, true),
      StructField("vote", StringType, true)
    )
  )

}
