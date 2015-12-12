import java.util.UUID

import twitter4j.Status

trait Filters {

  def getTweetFromStatus(status: Status): Tweet = {
    val strippedText: String = NormalizeSupport.normalize(status.getText)
    val sortedStrippedText: String = strippedText.sorted

    Tweet(UUID.randomUUID(), status.getId, new java.sql.Timestamp(status.getCreatedAt.getTime), status.getText, strippedText,
      sortedStrippedText, status.getUser.getId, status.getUser.getScreenName)
  }

  def statusFilter(status: Status): Boolean = {

    def isNotARetweet(status: Status): Boolean = {
      !status.isRetweet
    }

    def isEnglish(status: Status): Boolean = {
      status.getLang == "en"
    }

    def containsNoUrls(status: Status): Boolean = {
      status.getURLEntities.isEmpty &&
        status.getMediaEntities.isEmpty &&
        status.getExtendedMediaEntities.isEmpty
    }

    def hasNoHashtag(status: Status): Boolean = {
      status.getHashtagEntities.isEmpty
    }

    def noMentions(status: Status): Boolean = {
      status.getUserMentionEntities.isEmpty
    }

    def notWeatherUpdateBot(status: Status): Boolean = {
      !status.getText.startsWith("Get Weather Updates")
    }

    val filters = Seq[Status => Boolean](
      isEnglish,
      isNotARetweet,
      containsNoUrls,
      hasNoHashtag,
      noMentions,
      notWeatherUpdateBot
    )

    filters.forall(x => x(status))
  }

  def tweetFilter(tweet: Tweet): Boolean = {

    def isLongEnough(tweet: Tweet): Boolean = {
      tweet.tweetStrippedText.length > 8
    }

    val filters = Seq[Tweet => Boolean] (
      isLongEnough
    )

    filters.forall(x => x(tweet))
  }

  def isGoodMatch(anagramMatch: AnagramMatch): Boolean = {
    anagramMatch.lcsLengthToTotalLengthRatio > 0 &&
    anagramMatch.editDistanceToLengthRatio > 0 &&
    anagramMatch.wordCountDifference > 0 &&
    anagramMatch.isSameRearranged != IsSameWhenRearrangedEnum.TRUE
  }
}

object Filters extends Filters