package com.lansalo.ml.tree

import java.io.File

import com.lansalo.test.util._
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.tuning.CrossValidatorModel
import org.scalatest.{BeforeAndAfter, FunSuite}

class VoteIntentionTrainingTest extends FunSuite with BeforeAndAfter with SharedSparkContext {

  import VoteIntentionTrainingTestFixture._

  before {
    delete(new File("./target"), "fitted-model")
    delete(new File("./target"), "tuned-fitted-model")
  }

  test(s"training a model without tuning and save it in $ModelPath") {
    val model: PipelineModel = VoteIntentionTraining.training(trainingDF)
    model.save(ModelPath)
  }

  test(s"tuning a model and save it in $TunedModelPath") {
    val model: CrossValidatorModel = VoteIntentionTraining.tuning(trainingDF)
    model.save(TunedModelPath)
  }

  test("train and evaluate model") {
    val fitted: PipelineModel = VoteIntentionTraining.training(trainingDF)
    fitted.save(ModelPath)
    val model = PipelineModel.load(ModelPath)
    VoteIntentionTraining.validation(model, validationDF)
  }

}

object VoteIntentionTrainingTestFixture extends SharedSparkContext {

  import com.lansalo.ml.feature.{labelledDataSchema}

  val ModelPath: String = "./target/fitted-model"
  val TunedModelPath: String = "./target/tuned-fitted-model"

  val trainingDF = spark.read
    .option("delimiter", "\t")
    .option("header", "true")
    .schema(labelledDataSchema)
    .csv("src/test/resources/training.tsv")

  val validationDF = spark.read
    .option("delimiter", "\t")
    .option("header", "true")
    .schema(labelledDataSchema)
    .csv("src/test/resources/validation.tsv")

}
