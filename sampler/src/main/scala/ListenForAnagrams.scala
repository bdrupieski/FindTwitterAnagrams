import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.scalalogging.slf4j.StrictLogging
import twitter4j._

import scala.concurrent.duration._

object ListenForAnagrams extends StrictLogging with TweetDatabase {

  def main(args: Array[String]) {
    val saveTweetsToDatabaseListener = new StatusListener() {

      var totalCount: AtomicInteger = new AtomicInteger()
      var savedTweets: AtomicInteger = new AtomicInteger()

      def onStatus(status: Status) {
        totalCount.incrementAndGet()

        if (totalCount.get() % 10000 == 0) {
          logger.info(s"Processed $totalCount total tweets. Saved $savedTweets so far.")
        }

        FindAndSaveMatches.processStatus(status, savedTweets)
      }

      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {
        logger.info(s"status deleted: ${statusDeletionNotice.getStatusId}")
      }

      def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {
        logger.warn(s"track limitation notice: $numberOfLimitedStatuses")
      }

      def onException(ex: Exception): Unit = {
        logger.error("status listener error:")
        logger.error(ex.getMessage)
        logger.error(ex.getStackTraceString)
      }

      def onScrubGeo(arg0: Long, arg1: Long): Unit = {
        logger.info(s"geo scrubbed: $arg0, $arg1")
      }

      def onStallWarning(warning: StallWarning): Unit = {
        logger.warn(s"stall warning: ${warning.toString}")
      }
    }

    try {
      val twitterStream = new TwitterStreamFactory(TwitterApiConfigUtil.config).getInstance
      twitterStream.addListener(saveTweetsToDatabaseListener)
      twitterStream.sample("en")

      while (true){
        Thread.sleep(5.days.toMillis)
      }

      twitterStream.cleanUp()
      twitterStream.shutdown()
    } finally {
      tweetsDb.close
    }
  }
}
