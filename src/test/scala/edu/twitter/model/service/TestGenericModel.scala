package edu.twitter.model.service

import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.dto.Label

class TestGenericModel(val fixedLabel: Label, val name: String) extends GenericModel {
  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */
  override def getLabel(tweetText: String): Label = fixedLabel

}