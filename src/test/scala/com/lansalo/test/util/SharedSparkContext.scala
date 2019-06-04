package com.lansalo.test.util

import org.apache.spark.sql.SparkSession

trait SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder()
    .config("spark.ui.showConsoleProgress", false)
    .master("local[1]")
    .getOrCreate()
}
