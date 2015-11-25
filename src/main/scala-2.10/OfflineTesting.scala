import java.io.{FileInputStream, ObjectInputStream, FileOutputStream, ObjectOutputStream}

import twitter4j._
import slick.driver.H2Driver.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object OfflineTesting {
  def main(args: Array[String]) {
    sampleAndSaveStatusesToFile()
    loadStatusesFromFileAndSaveToDbAsTweets()
  }

  def sampleAndSaveStatusesToFile(): Unit = {
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
    twitterStream.sample("en")
    Thread.sleep(30000)

    twitterStream.cleanUp()
    twitterStream.shutdown()

    val objectStream = new ObjectOutputStream(new FileOutputStream("statuses"))
    println(statusCollector.statuses.size)

    objectStream.writeObject(statusCollector.statuses)
    objectStream.close()
  }

  def loadStatusesFromFileAndSaveToDbAsTweets(): Unit = {
    TweetDatabaseConfig.initTables()
    val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]

    def load(filename: String): ArrayBuffer[twitter4j.Status] = {
      val objectStream = new ObjectInputStream(new FileInputStream(filename))
      val statuses = objectStream.readObject().asInstanceOf[ArrayBuffer[twitter4j.Status]]
      objectStream.close()
      statuses
    }

    val statuses: ArrayBuffer[Status] = load("statuses")
    val filteredStatuses = statuses.filter(Filters.statusFilter)

    val tweetsToInsert: ArrayBuffer[Tweet] = filteredStatuses
      .map(Filters.getTweetCase)
      .filter(Filters.tweetFilter)
    val tweetInserts = tweetsTable ++= tweetsToInsert

    try {
      Await.result(TweetDatabaseConfig.db.run(DBIO.seq(tweetInserts)), Duration.Inf)
    } finally TweetDatabaseConfig.db.close
  }
}