import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object TweetDatabaseConfig {

  val db = Database.forConfig("h2tweetdb", ConfigFactory.load("db"))
  val log = LoggerFactory.getLogger(TweetDatabaseConfig.getClass)

  def initTables(): Unit = {

    val tweets: TableQuery[Tweets] = TableQuery[Tweets]
    val anagramMatches: TableQuery[AnagramMatches] = TableQuery[AnagramMatches]

    try {
      val tables: List[MTable] = Await.result(db.run(MTable.getTables), Duration.Inf).toList

      if (tables.isEmpty) {
        log.info("No tables. Creating schema.")
        Await.result(db.run(
          DBIO.seq(
            (tweets.schema ++ anagramMatches.schema).create
          )
        ), Duration.Inf)
      }

    } catch {
      case e: Exception =>
        db.close
        log.error(e.getMessage)
        log.error(e.getStackTraceString)
        throw e
    }
  }

  def main(args: Array[String]) {
    initTables()
  }
}
