package com.lansalo.ml.tree

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.param.Param
import org.apache.spark.ml.tuning.{CrossValidator, CrossValidatorModel, ParamGridBuilder}
import org.apache.spark.sql.DataFrame

object VoteIntentionTraining {

  val vectorAssembler = new VectorAssembler().setInputCols(Array("f_gender", "age", "f_area", "income", "f_education"))
    .setOutputCol("features")
  // setHandleInvalid = keep or error or skip
  val genderIndexer = new StringIndexer().setInputCol("gender").setOutputCol("f_gender")//.setHandleInvalid("keep")
  val areaIndexer = new StringIndexer().setInputCol("area").setOutputCol("f_area")//.setHandleInvalid("keep")
  val educationIndexer = new StringIndexer().setInputCol("education").setOutputCol("f_education")//.setHandleInvalid("keep")
  val voteIntentionIndexer = new StringIndexer().setInputCol("vote").setOutputCol("label")//.setHandleInvalid("keep")

  def training(trainingSet: DataFrame): PipelineModel = {
    val classifier = new RandomForestClassifier()
      .setNumTrees(24) // default value
      .setMaxDepth(7)  // default value
      //.setMaxBins(32)  // default value
      .setSeed(5043) // set the seed for reproducibility

    val trainingPipeline = new Pipeline().setStages(
      Array(genderIndexer, areaIndexer, educationIndexer, voteIntentionIndexer, vectorAssembler, classifier))
    trainingPipeline.fit(trainingSet)
  }

  def tuning(trainingSet: DataFrame): CrossValidatorModel = {

    val rfClassifier = new RandomForestClassifier()
    val pipeline= new Pipeline().setStages(
      Array(genderIndexer, areaIndexer, educationIndexer, voteIntentionIndexer, vectorAssembler, rfClassifier))

    val nFolds: Int = 8
    val metric: String = "f1"
    val paramGrid = new ParamGridBuilder()
      .addGrid(rfClassifier.numTrees, Array(15, 20, 25))
      .addGrid(rfClassifier.maxDepth, Array(4, 5, 6))
      //.addGrid(rfClassifier.maxBins, Array(30, 32, 34))
      .build()
    val evaluator = new MulticlassClassificationEvaluator() .setLabelCol("label") .setPredictionCol("prediction") .setMetricName(metric)

    val cv: CrossValidator = new CrossValidator()
      .setEstimator(pipeline)
      .setEstimatorParamMaps(paramGrid)
      .setEvaluator(evaluator)
      .setNumFolds(nFolds)
      .setParallelism(3)

    val fittedPipeline: CrossValidatorModel = cv.fit(trainingSet)
    val best: PipelineModel = fittedPipeline.bestModel.asInstanceOf[PipelineModel]

    val paramMap = fittedPipeline.getEstimatorParamMaps
      .zip(fittedPipeline.avgMetrics)
      .maxBy(_._2)
      ._1

    // We are interested in stage 6 (so the 5th element in the array of stages)
    // because the pipeline (as defined in this example) has 6 stages, the last
    // one being the classifier (the one we are interested in)
    println(s"@@@@ best num trees:     ${best.stages(5).asInstanceOf[RandomForestClassificationModel].getNumTrees}")
    println(s"@@@@ best max depth:     ${best.stages(5).asInstanceOf[RandomForestClassificationModel].getMaxDepth}")
    //println(s"@@@@ best max bins:      ${best.stages(5).asInstanceOf[RandomForestClassificationModel].getMaxBins}")

    // Also paramMap will return the optimal values
    println(s"@@@@ best paramMap:      ${paramMap}")

    fittedPipeline
  }

  def validation(model: PipelineModel, validation: DataFrame) = {
    // Make predictions.
    val predictions = model.transform(validation)

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("f1") // "f1" = default, other options include "weightedPrecision", "weightedRecall", "accuracy"

    val f1: Double = evaluator.evaluate(predictions)
    println("@@ F1 ====> " + f1)
  }

  private def printParam(param: Param[_]): Unit = {
    println(s"@@~~ Param name: ${param.name}, doc = ${param.doc}, class = ${param.getClass.getName}, string = ${param.toString()}")
  }

}
