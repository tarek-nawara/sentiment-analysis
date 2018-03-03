package edu.twitter.model.impl

import edu.twitter.model.api.GenericModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel

/**
  * Wrapper for GradientBoosting Model
  *
  * @param model actual model
  */
@SerialVersionUID(1L)
class GradientBoostingModel(model: GradientBoostedTreesModel) extends GenericModel with Serializable {

  val name = "GradientBoosting"
  val hashingTF = new HashingTF(2000)
  val invalidTokens = Set("http", "@", "rt", "#", "RT")

  override def getLabel(tweetText: String): Double = {
    val tokens = tweetText.split(" ").filter(!invalidTokens(_))
    val features = hashingTF.transform(tokens)
    model.predict(features)
  }
}
