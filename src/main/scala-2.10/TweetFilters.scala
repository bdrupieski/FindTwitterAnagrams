import java.io.{FileInputStream, ObjectInputStream}
import java.util.UUID

import slick.driver.H2Driver.api._
import twitter4j.Status

import scala.collection.immutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.Await

object TweetFilters {

  private val lowercaseAlphanumericCharacters = HashSet[Char]("abcdefghijklmnopqrstuvwxyz1234567890".toCharArray: _*)

  def getTweetCase(status: Status): Tweet = {
    val strippedText: String = status.getText.toLowerCase.filter(y => lowercaseAlphanumericCharacters.contains(y))
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

  def tweetFilter(tweet: Tweet): Boolean = {

    def isLongEnough(tweet: Tweet): Boolean = {
      tweet.tweetStrippedText.length > 8
    }

    val filters = Seq[Tweet => Boolean] (
      isLongEnough
    )

    filters.forall(x => x(tweet))
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

    val tweetsToInsert: ArrayBuffer[Tweet] = filteredStatuses.map(getTweetCase).filter(tweetFilter)
    val tweetInserts = tweetsTable ++= tweetsToInsert

    try {
      Await.result(TweetDatabaseConfig.db.run(DBIO.seq(tweetInserts)), Duration.Inf)
    } finally TweetDatabaseConfig.db.close
  }
}
