package edu.twitter.classification

import edu.twitter.model.impl.TweetTextFilter
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CleanTweetSuite extends FunSuite {
  test("applying filtering over the tweet text should remove references and mentions") {
    val tweet = "FlemingYoung happy bday to boy C https://tco/LmjhzggruZ"
    val res = TweetTextFilter.filterTweet(tweet)
    val filteredTweet = "FlemingYoung happy bday to boy C  <URL> "
    assert(res == filteredTweet)
  }

  test("filter Retweeted tweets") {
    val tweet = "RT this is a retweeted tweet"
    assert(tweet.startsWith("RT "))
    val validOne = "RTtweet this is a valid tweet"
    assert(!validOne.startsWith("RT "))
  }
}
