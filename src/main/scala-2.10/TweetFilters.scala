import java.io.{FileInputStream, ObjectInputStream}
import java.util.UUID

import slick.driver.H2Driver.api._
import twitter4j.Status

import scala.collection.immutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object TweetFilters {

  private val anagrammableCharacters = HashSet[Char]("abcdefghijklmnopqrstuvwxyz1234567890".toCharArray: _*)

  def getTweetCase(status: Status): Tweet = {
    val strippedText: String = status.getText.toLowerCase.filter(y => anagrammableCharacters.contains(y))
    val sortedStrippedText: String = strippedText.sorted

    Tweet(UUID.randomUUID(), status.getId, new java.sql.Timestamp(status.getCreatedAt.getTime), status.getText, strippedText,
      sortedStrippedText, status.getUser.getId, status.getUser.getScreenName)
  }

  def statusFilter(status: Status): Boolean = {

    def isNotARetweet(status: Status): Boolean = {
      !status.isRetweet
    }

    def isEnglish(status: Status): Boolean = {
      status.getLang == "en"
    }

    def containsNoLink(status: Status): Boolean = {
      !status.getText.contains("http")
    }

    def hasNoHashtag(status: Status): Boolean = {
      !status.getText.contains("#")
    }

    def notAtAnybody(status: Status): Boolean = {
      !status.getText.contains("@")
    }

    def notWeatherUpdateBot(status: Status): Boolean = {
      !status.getText.startsWith("Get Weather Updates")
    }

    val filters = Seq[Status => Boolean](
      isEnglish,
      isNotARetweet,
      containsNoLink,
      hasNoHashtag,
      notAtAnybody,
      notWeatherUpdateBot
    )

    filters.forall(x => x(status))
  }

  def strippedTextFilter(strippedText: String): Boolean = {

    def isLongEnough(strippedText: String): Boolean = {
      strippedText.length > 8
    }

    val filters = Seq[String => Boolean] (
      isLongEnough
    )

    filters.forall(x => x(strippedText))
  }

  // TODO: look into more robust tokenization
  def tokenizeTweetText(originalText: String): Array[String] = {
    val formattedText = originalText.toLowerCase.replaceAll("'", "").replaceAll("[^a-z0-9 ]+", " ")

    val words = formattedText.trim.split("\\s+")

    words
  }

  def getWordCountDifference(tweet1OriginalText: String, tweet2OriginalText: String): (Int, Int) = {

    def getWordCount(tweetOriginalText: String): Map[String, Int] = {
      tokenizeTweetText(tweetOriginalText).groupBy(x => x).map(x => (x._1, x._2.length))
    }

    val tweet1Counts = getWordCount(tweet1OriginalText)
    val tweet2Counts = getWordCount(tweet2OriginalText)

    val allWordsInBothTweets = tweet1Counts.keySet ++ tweet2Counts.keySet

    var wordDifferenceCount = 0

    for (word <- allWordsInBothTweets) {
      val countInTweet1 = tweet1Counts.getOrElse(word, 0)
      val countInTweet2 = tweet2Counts.getOrElse(word, 0)

      wordDifferenceCount += math.abs(countInTweet1 - countInTweet2)
    }

    val totalWords = tweet1Counts.values.sum + tweet2Counts.values.sum

    (wordDifferenceCount, totalWords)
  }

  // loads tweets from a file, filters them, and puts them in a database
  // For offline testing
  def main(args: Array[String]) {

    TweetDatabaseConfig.initTables()
    val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]

    def load(filename: String): ArrayBuffer[twitter4j.Status] = {
      val objectStream = new ObjectInputStream(new FileInputStream(filename))
      val statuses = objectStream.readObject().asInstanceOf[ArrayBuffer[twitter4j.Status]]
      objectStream.close()
      statuses
    }

    val statuses: ArrayBuffer[Status] = load("statuses")
    val filteredStatuses = statuses.filter(statusFilter)
    println(s"Filtered: ${filteredStatuses.size}, Unfiltered: ${statuses.size}")

    val tweetsToInsert: ArrayBuffer[Tweet] = filteredStatuses
      .map(getTweetCase)
      .filter(x => strippedTextFilter(x.tweetStrippedText))

    val tweetInserts = tweetsTable ++= tweetsToInsert

    try {
      val insertFuture: Future[Unit] = TweetDatabaseConfig.db.run(DBIO.seq(tweetInserts))
      Await.result(insertFuture, Duration.Inf)
    } finally TweetDatabaseConfig.db.close
  }
}
