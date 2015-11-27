import java.util.concurrent.atomic.AtomicInteger

import slick.dbio.{NoStream, DBIOAction}
import slick.driver.H2Driver.api._

import twitter4j._
import AnagramMatchBuilder._
import Filters._

import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
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
          val newTweet = Filters.getTweetCase(status)
          if (Filters.tweetFilter(newTweet)) {

            log.debug(s"processing (${savedTweets.get()}): ${newTweet.tweetOriginalText} (${newTweet.tweetSortedStrippedText})")
            val tweetMatchQuery = tweetsTable.filter(x => x.tweetSortedStrippedText === newTweet.tweetSortedStrippedText)
            val tweetInsert = tweetsTable += newTweet

            TweetDatabaseConfig.db.run(tweetMatchQuery.result) map { (tweets: Seq[Tweet]) =>

              val sameTweetsAlreadySavedToDb = tweets.filter(x => x.tweetStrippedText == newTweet.tweetStrippedText)

              if (sameTweetsAlreadySavedToDb.nonEmpty) {
                log.info(s"ALREADY SAVED: ${newTweet.tweetOriginalText} (${newTweet.tweetStrippedText}) " +
                  s"EXISTING DUPLICATE(S): ${sameTweetsAlreadySavedToDb.map(x => s"${x.tweetOriginalText} (${x.tweetStrippedText})").mkString(" ")}")
              } else {

                val inserts = ListBuffer[DBIOAction[_, NoStream, Effect.Write]](tweetInsert)

                if (tweets.nonEmpty) {
                  val anagramMatches = tweets.map(buildAnagramMatch(newTweet, _)).filter(isGoodMatch)
                  if (anagramMatches.nonEmpty) {
                    val anagramMatchInserts = anagramMatchesTable ++= anagramMatches
                    inserts += anagramMatchInserts
                    log.info(s"MATCH ON ${newTweet.tweetOriginalText}:" +
                      s" ${anagramMatches.map(x => s"IF: ${x.interestingFactor}").mkString(" ")}")
                  }
                }

                log.debug(s"inserting (${savedTweets.get()}): ${newTweet.tweetOriginalText}")
                TweetDatabaseConfig.db.run(DBIO.seq(inserts: _*).transactionally)
                savedTweets.incrementAndGet()
              }
            }
          }
        }
      }
      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
        log.info(s"STATUS DELETED: ${statusDeletionNotice.getStatusId}")
      }
      def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {
        log.warn(s"TRACK LIMITATION NOTICE: $numberOfLimitedStatuses")
      }
      def onException(ex: Exception): Unit = {
        log.error(ex.getMessage)
        log.error(ex.getStackTraceString)
      }
      def onScrubGeo(arg0: Long, arg1: Long): Unit = {
        log.info(s"GEO SCRUBBED: $arg0, $arg1")
      }
      def onStallWarning(warning: StallWarning): Unit = {
        log.warn(s"STALL WARNING: ${warning.toString}")
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
