import java.util.concurrent.atomic.AtomicInteger

import slick.driver.H2Driver.api._
import twitter4j._
import MatchMetrics._

import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object SaveTweetsToDatabase {

  val log = LoggerFactory.getLogger(SaveTweetsToDatabase.getClass)

  def main(args: Array[String]) {
    TweetDatabaseConfig.initTables()

    val saveTweetsToDatabaseListener = new StatusListener() {

      var totalCount: AtomicInteger = new AtomicInteger()
      var savedTweets: AtomicInteger = new AtomicInteger()
      val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]
      val anagramMatchesTable: TableQuery[AnagramMatches] = TableQuery[AnagramMatches]

      def onStatus(status: Status) {
        totalCount.incrementAndGet()

        if (totalCount.get() % 1000 == 0) {
          log.info(s"Processed $totalCount total tweets. Saved $savedTweets so far.")
        }

        if (Filters.statusFilter(status)) {
          val tweet = Filters.getTweetCase(status)
          if (Filters.tweetFilter(tweet)) {

            log.debug(s"processing: ${tweet.tweetOriginalText}")
            val tweetMatchQuery = tweetsTable.filter(x => x.tweetSortedStrippedText === tweet.tweetSortedStrippedText)
            val tweetInsert = tweetsTable += tweet
            savedTweets.incrementAndGet()

            TweetDatabaseConfig.db.run(tweetMatchQuery.result) map { (x: Seq[Tweet]) =>
              val actions = if (x.nonEmpty) {
                val matchingTweet = x.head
                val originalTextEditDistance = demerauLevenshteinDistance(tweet.tweetOriginalText, matchingTweet.tweetOriginalText)
                val strippedTextEditDistance = demerauLevenshteinDistance(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)
                val hammingDistanceStrippedText = hammingDistance(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)
                val lcsLengthStrippedText = longestCommonSubstring(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)
                val (wordCountDifference, totalWords) = getWordCountDifference(
                  tweet.tweetOriginalText, matchingTweet.tweetOriginalText)
                val isSameRearranged = MatchMetrics.isMatchWhenWordsRearranged(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)

                val length = tweet.tweetSortedStrippedText.length
                val inverseLcsLengthToLengthRatio = 1 - (lcsLengthStrippedText.toFloat / length)
                val editDistanceToLengthRatio = strippedTextEditDistance.toFloat / length
                val diffWordCountToTotalWordCountRatio = wordCountDifference.toFloat / totalWords
                val interestingFactor = (inverseLcsLengthToLengthRatio + editDistanceToLengthRatio + diffWordCountToTotalWordCountRatio) / 3.toFloat

                val anagramMatch = AnagramMatch(0, tweet.id, matchingTweet.id,
                  originalTextEditDistance, strippedTextEditDistance, hammingDistanceStrippedText,
                  lcsLengthStrippedText, wordCountDifference, totalWords,
                  inverseLcsLengthToLengthRatio, editDistanceToLengthRatio, diffWordCountToTotalWordCountRatio,
                  isSameRearranged, interestingFactor)

                val anagramInsert = anagramMatchesTable += anagramMatch

                log.info(s"MATCH! [${tweet.tweetOriginalText}] and [${x.head.tweetOriginalText}] " +
                  s"IF: $interestingFactor, # of matches: ${x.size}")

                DBIO.seq(tweetInsert, anagramInsert).transactionally
              } else {
                DBIO.seq(tweetInsert)
              }

              TweetDatabaseConfig.db.run(actions)
            }
          }
        }
      }
      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = { }
      def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = { }
      def onException(ex: Exception): Unit = {
        ex.printStackTrace()
        log.error(ex.getMessage)
        log.error(ex.getStackTraceString)
      }
      def onScrubGeo(arg0: Long, arg1: Long): Unit = { }
      def onStallWarning(warning: StallWarning): Unit = {
        log.warn("STALL WARNING!")
        log.warn(warning.toString)
      }
    }

    try {
      val twitterStream = new TwitterStreamFactory(TwitterApiConfigUtil.config).getInstance
      twitterStream.addListener(saveTweetsToDatabaseListener)
      twitterStream.sample("en")
      Thread.sleep(5.days.toMillis)

      twitterStream.cleanUp()
      twitterStream.shutdown()
    } finally {
      TweetDatabaseConfig.db.close
    }
  }
}
