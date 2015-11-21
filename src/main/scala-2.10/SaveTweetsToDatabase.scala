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

      var totalCount: Int = 0
      var savedTweets: Int = 0
      val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]
      val anagramMatchesTable: TableQuery[AnagramMatches] = TableQuery[AnagramMatches]

      def onStatus(status: Status) {
        totalCount += 1

        if (totalCount % 1000 == 0) {
          log.info(s"Processed $totalCount total tweets. Saved $savedTweets so far.")
        }

        if (TweetFilters.statusFilter(status)) {
          val tweet = TweetFilters.getTweetCase(status)
          if (TweetFilters.strippedTextFilter(tweet.tweetStrippedText)) {

            log.debug(s"processing: ${tweet.tweetOriginalText}")
            val tweetMatchQuery = tweetsTable.filter(x => x.tweetSortedStrippedText === tweet.tweetSortedStrippedText)
            val tweetInsert = tweetsTable += tweet
            savedTweets += 1

            TweetDatabaseConfig.db.run(tweetMatchQuery.result) map { (x: Seq[Tweet]) =>
              val actions = if (x.nonEmpty) {
                log.info(s"MATCH! [${tweet.tweetOriginalText}] and [${x.head.tweetOriginalText}]")
                val matchingTweet = x.head
                val originalTextEditDistance = demerauLevenshteinDistance(tweet.tweetOriginalText, matchingTweet.tweetOriginalText)
                val strippedTextEditDistance = demerauLevenshteinDistance(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)
                val hammingDistanceStrippedText = hammingDistance(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)
                val lcsLengthStrippedText = longestCommonSubstring(tweet.tweetStrippedText, matchingTweet.tweetStrippedText)
                val wordCountDifference = TweetFilters.getWordCountDifference(tweet.tweetOriginalText, matchingTweet.tweetOriginalText)
                val anagramMatch = AnagramMatch(0, tweet.id, matchingTweet.id,
                  originalTextEditDistance, strippedTextEditDistance, hammingDistanceStrippedText,
                  lcsLengthStrippedText, wordCountDifference)
                val anagramInsert = anagramMatchesTable += anagramMatch

                DBIO.seq(tweetInsert, anagramInsert)
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
