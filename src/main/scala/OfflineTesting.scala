import java.io._

import slick.driver.PostgresDriver.api._
import twitter4j._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex

object OfflineTesting extends Filters with TweetDatabase {
  def main(args: Array[String]) {
    sampleAndSaveStatusesToFile()
    loadStatusesFromFileAndSaveToDbAsTweets()
    dumpTweetsToFile()
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
    val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]

    def load(filename: String): ArrayBuffer[twitter4j.Status] = {
      val objectStream = new ObjectInputStream(new FileInputStream(filename))
      val statuses = objectStream.readObject().asInstanceOf[ArrayBuffer[twitter4j.Status]]
      objectStream.close()
      statuses
    }

    val statuses: ArrayBuffer[Status] = load("statuses")
    val filteredStatuses = statuses.filter(statusFilter)

    val tweetsToInsert: ArrayBuffer[Tweet] = filteredStatuses
      .map(getTweetFromStatus)
      .filter(tweetFilter)
    val tweetInserts = tweetsTable ++= tweetsToInsert

    try {
      Await.result(tweetsDb.run(DBIO.seq(tweetInserts)), Duration.Inf)
    } finally tweetsDb.close
  }

  def dumpTweetsToFile(): Unit = {
    val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]
    val tweets = Await.result(tweetsDb.run(tweetsTable.result), Duration.Inf)

    val newlineRegex = new Regex("[\\r\\n]+")
    val tweetGroups = tweets.grouped(800000)

    var i = 0
    for (group <- tweetGroups) {
      val fw = new FileWriter(s"tweets-$i.tsv", true)
      try {
        for (x <- group) {
          val escaped = newlineRegex.replaceAllIn(x.tweetOriginalText.replaceAll("\\\\", ""), "\\\\r\\\\n")
          fw.write(s"${x.id}\t${x.statusId}\t${x.createdAt}\t$escaped\t${x.tweetStrippedText}\t${x.tweetSortedStrippedText}\t${x.userId}\t${x.userName}\t${x.isMatched}${sys.props("line.separator")}")
        }
      } finally fw.close()
      i = i + 1
    }
  }
}