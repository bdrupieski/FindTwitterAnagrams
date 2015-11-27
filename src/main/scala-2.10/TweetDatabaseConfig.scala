import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.StrictLogging
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object TweetDatabaseConfig extends StrictLogging {

  val db = Database.forConfig("h2tweetdb", ConfigFactory.load("db"))

  def initTables(): Unit = {

    val tweets: TableQuery[Tweets] = TableQuery[Tweets]
    val anagramMatches: TableQuery[AnagramMatches] = TableQuery[AnagramMatches]

    try {
      val tables: List[MTable] = Await.result(db.run(MTable.getTables), Duration.Inf).toList

      if (tables.isEmpty) {
        logger.info("No tables. Creating schema.")
        Await.result(db.run(
          DBIO.seq(
            (tweets.schema ++ anagramMatches.schema).create
          )
        ), Duration.Inf)
      }

    } catch {
      case e: Exception =>
        db.close
        logger.error(e.getMessage)
        logger.error(e.getStackTraceString)
        throw e
    }
  }

  def main(args: Array[String]) {
    initTables()
  }
}
