import java.util.UUID

import matching._
import models._
import tables._

import com.typesafe.scalalogging.slf4j.StrictLogging
import slick.dbio.{DBIOAction, NoStream}
import slick.driver.PostgresDriver.api._
import twitter4j.Status
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

object FindAndSaveMatches extends StrictLogging with Filters with MatchMetrics with TweetDatabase {

  val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]
  val anagramMatchesTable: TableQuery[AnagramMatches] = TableQuery[AnagramMatches]

  def processStatus(status: Status): Boolean = {
    var savedTweet = false

    if (statusFilter(status)) {
      val newTweet = buildTweet(status)
      if (tweetFilter(newTweet)) {

        logger.debug(s"processing ${newTweet.tweetOriginalText} (${newTweet.tweetSortedStrippedText})")

        val tweetMatchQuery = tweetsTable.filter(x => x.tweetSortedStrippedText === newTweet.tweetSortedStrippedText)
        val tweetInsert = tweetsTable += newTweet

        tweetsDb.run(tweetMatchQuery.result) map { (tweets: Seq[Tweet]) =>

          val sameTweetsAlreadySavedToDb = tweets.filter(x => x.tweetStrippedText == newTweet.tweetStrippedText)

          if (sameTweetsAlreadySavedToDb.nonEmpty) {
            logger.debug(s"already saved: ${newTweet.tweetOriginalText} " +
              s"existing duplicate(s): ${sameTweetsAlreadySavedToDb.map(x => s"${x.tweetOriginalText} (${x.tweetStrippedText})").mkString(" ")}")
          } else {
            val inserts = ListBuffer[DBIOAction[_, NoStream, Effect.Write]](tweetInsert)

            if (tweets.nonEmpty) {
              val anagramMatches = tweets.map(buildAnagramMatch(newTweet, _)).filter(isGoodMatch)
              if (anagramMatches.nonEmpty) {
                val anagramMatchInserts = anagramMatchesTable ++= anagramMatches
                inserts += anagramMatchInserts
                logger.info(s"MATCH ON ${newTweet.tweetOriginalText}:" +
                  s" ${anagramMatches.map(x => s"IF: ${x.interestingFactor}").mkString(" ")}")
              }
            }

            logger.debug(s"inserting: ${newTweet.tweetOriginalText}")
            tweetsDb.run(DBIO.seq(inserts: _*).transactionally)
            savedTweet = true
          }
        }
      }
    }

    savedTweet
  }

  def buildTweet(status: Status): Tweet = {
    val strippedText: String = NormalizeSupport.normalize(status.getText)
    val sortedStrippedText: String = strippedText.sorted

    Tweet(UUID.randomUUID(), status.getId, new java.sql.Timestamp(status.getCreatedAt.getTime), status.getText, strippedText,
      sortedStrippedText, status.getUser.getId, status.getUser.getScreenName)
  }

  def buildAnagramMatch(tweet1: Tweet, tweet2: Tweet): AnagramMatch = {
    val originalTextEditDistance = demerauLevenshteinDistance(tweet1.tweetOriginalText, tweet2.tweetOriginalText)
    val strippedTextEditDistance = demerauLevenshteinDistance(tweet1.tweetStrippedText, tweet2.tweetStrippedText)
    val hammingDistanceStrippedText = hammingDistance(tweet1.tweetStrippedText, tweet2.tweetStrippedText)
    val lcsLengthStrippedText = longestCommonSubstring(tweet1.tweetStrippedText, tweet2.tweetStrippedText)
    val (wordCountDifference, totalWords) = getWordCountDifference(tweet1.tweetOriginalText, tweet2.tweetOriginalText)
    val isSameRearranged = isMatchWhenWordsRearranged(tweet1.tweetStrippedText, tweet2.tweetStrippedText)

    val length = tweet1.tweetSortedStrippedText.length
    val inverseLcsLengthToLengthRatio = 1 - (lcsLengthStrippedText.toFloat / length)
    val editDistanceToLengthRatio = strippedTextEditDistance.toFloat / length
    val diffWordCountToTotalWordCountRatio = wordCountDifference.toFloat / totalWords
    val interestingFactor = (inverseLcsLengthToLengthRatio + editDistanceToLengthRatio + diffWordCountToTotalWordCountRatio) / 3.toFloat

    AnagramMatch(0, tweet1.id, tweet2.id, originalTextEditDistance, strippedTextEditDistance,
      hammingDistanceStrippedText, lcsLengthStrippedText, wordCountDifference, totalWords,
      inverseLcsLengthToLengthRatio, editDistanceToLengthRatio, diffWordCountToTotalWordCountRatio, isSameRearranged,
      interestingFactor)
  }
}
