package edu.twitter.model.impl.neuralnetwork.normal

import edu.twitter.model.api.GenericModel
import edu.twitter.model.client.dto.Label
import edu.twitter.model.impl.TweetTextFilter
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.LowCasePreProcessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.{INDArrayIndex, NDArrayIndex}

/**
  * Wrapper for Neural Network Model
  *
  * @param model       actual model
  * @param wordVectors word2vec model used to extract the features
  */
class NeuralNetworkModel(model: MultiLayerNetwork, wordVectors: WordVectors) extends GenericModel {
  val name: String = NeuralNetworkModel.name
  private val vectorSize = wordVectors.getWordVector(wordVectors.vocab.wordAtIndex(0)).length

  /**
    * Classify the given tweet.
    *
    * @param tweetText target tweet message for classification.
    * @return 0 for sad & 1 for happy
    */

  override def getLabel(tweetText: String): Label = {
    val filteredTweet = TweetTextFilter.filterTweet(tweetText)
    val features = loadFeaturesFromString(filteredTweet)
    val networkOutput = synchronized {
      model.output(features)
    }
    val timeSeriesLength = networkOutput.size(2)
    val probabilities = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(timeSeriesLength - 1))
    val happy = probabilities.getDouble(0)
    val sad = probabilities.getDouble(1)
    if (happy > sad) Label.HAPPY else Label.SAD
  }

  /**
    * Used post training to convert a String to a features INDArray that can be passed to the network output method
    *
    * @param reviewContents Contents of the review to vectorize
    * @return Features array for the given input String
    */
  def loadFeaturesFromString(reviewContents: String): INDArray = {

    val tokenizerFactory: TokenizerFactory = new DefaultTokenizerFactory
    tokenizerFactory.setTokenPreProcessor(new LowCasePreProcessor)

    import scala.collection.JavaConversions._
    val tokens = tokenizerFactory.create(reviewContents).getTokens
    val tokensFiltered = tokens.filter(wordVectors.hasWord)

    val features = Nd4j.create(1, vectorSize, math.max(tokensFiltered.size, 1))
    for (j <- 0 until tokensFiltered.size) {
      val token = tokensFiltered.get(j)
      val vector = wordVectors.getWordVectorMatrix(token)
      features.put(Array[INDArrayIndex](NDArrayIndex.point(0), NDArrayIndex.all, NDArrayIndex.point(j)), vector)
    }
    features
  }


}


/** Companion object for the model
  * only holding the name. */
object NeuralNetworkModel {
  val name = "NeuralNetwork"
}
