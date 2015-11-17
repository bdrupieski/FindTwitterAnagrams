import java.io.{FileOutputStream, ObjectOutputStream}

import twitter4j._

import scala.collection.mutable.ArrayBuffer

// Samples tweets for a few seconds and saves them to a file for offline testing
object SaveTweetsToFile {
  def main(args: Array[String]) {

    val statusCollector = new StatusListener() {

      val statuses = new ArrayBuffer[Status]()

      def onStatus(status: Status) {
        statuses += status
        println(s"ScreenName: ${status.getUser.getScreenName} Language: ${status.getLang} Text: ${status.getText}")
      }

      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}
      def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}
      def onException(ex: Exception): Unit = { ex.printStackTrace() }
      def onScrubGeo(arg0: Long, arg1: Long): Unit = {}
      def onStallWarning(warning: StallWarning): Unit = {}
    }

    val twitterStream = new TwitterStreamFactory(TwitterApiConfigUtil.config).getInstance
    twitterStream.addListener(statusCollector)
    twitterStream.sample()
    Thread.sleep(30000)

    twitterStream.cleanUp()
    twitterStream.shutdown()

    val objectStream = new ObjectOutputStream(new FileOutputStream("statuses"))
    println(statusCollector.statuses.size)

    objectStream.writeObject(statusCollector.statuses)
    objectStream.close()
  }
}