import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

trait TweetDatabase {
  val tweetsDb: PostgresDriver.backend.DatabaseDef = Database.forConfig("database", ConfigFactory.load("db"))
}

object TweetDatabase extends TweetDatabase