import com.typesafe.config.ConfigFactory

object TwitterApiConfigUtil {
  private val appConfig = ConfigFactory.load("twitterapi.conf")

  val config = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey(appConfig.getString("twitter.consumerkey"))
    .setOAuthConsumerSecret(appConfig.getString("twitter.consumersecret"))
    .setOAuthAccessToken(appConfig.getString("twitter.accesstoken"))
    .setOAuthAccessTokenSecret(appConfig.getString("twitter.accesstokensecret"))
    .build
}
