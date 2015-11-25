import MatchMetrics._
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object UpdateMatchMetrics {
  def main(args: Array[String]) {

    val tweetsTable: TableQuery[Tweets] = TableQuery[Tweets]
    val anagramMatchesTable: TableQuery[AnagramMatches] = TableQuery[AnagramMatches]

    val db = TweetDatabaseConfig.db
    val f = db.stream(anagramMatchesTable.result).foreach(x => {
      val tweet1 = db.run(tweetsTable.filter(y => y.id === x.tweet1Id).result)
      val tweet2 = db.run(tweetsTable.filter(y => y.id === x.tweet2Id).result)

      val tweet1Result = Await.result(tweet1, Duration.Inf)
      val tweet2Result = Await.result(tweet2, Duration.Inf)

      val strippedTextEditDistance = demerauLevenshteinDistance(
        tweet1Result.head.tweetStrippedText, tweet2Result.head.tweetStrippedText)

      val hammingDistanceStrippedText = hammingDistance(
        tweet1Result.head.tweetStrippedText, tweet2Result.head.tweetStrippedText)

      val lcsLengthStrippedText = longestCommonSubstring(
        tweet1Result.head.tweetStrippedText, tweet2Result.head.tweetStrippedText)

      val (wordCountDifference, totalWords) = getWordCountDifference(
        tweet1Result.head.tweetOriginalText, tweet2Result.head.tweetOriginalText)

      val isSameRearranged = isMatchWhenWordsRearranged(
        tweet1Result.head.tweetOriginalText, tweet2Result.head.tweetOriginalText).id

      val length = tweet1Result.head.tweetSortedStrippedText.length
      val inverseLcsToLengthRatio = 1 - (lcsLengthStrippedText.toFloat / length)
      val editDistanceToLengthRatio = strippedTextEditDistance.toFloat / length
      val diffWordCountToTotalWordCountRatio = wordCountDifference.toFloat / totalWords

      val interestingFactor = (inverseLcsToLengthRatio + editDistanceToLengthRatio + diffWordCountToTotalWordCountRatio) / 3.toFloat

      val update =
        sqlu"""UPDATE ANAGRAM_MATCHES SET
               HAMMING_DISTANCE_STRIPPED_TEXT = $hammingDistanceStrippedText,
               LONGEST_COMMON_SUBSTRING_LENGTH_STRIPPED_TEXT = $lcsLengthStrippedText,
               WORD_COUNT_DIFFERENCE = $wordCountDifference,
               TOTAL_WORDS = $totalWords,
               INVERSE_LCS_LENGTH_TO_TOTAL_LENGTH_RATIO = $inverseLcsToLengthRatio,
               EDIT_DISTANCE_TO_LENGTH_RATIO = $editDistanceToLengthRatio,
               DIFFERENT_WORD_COUNT_TO_TOTAL_WORD_COUNT_RATIO = $diffWordCountToTotalWordCountRatio,
               IS_SAME_REARRANGED = $isSameRearranged,
               INTERESTING_FACTOR = $interestingFactor
               WHERE ID = ${x.id}"""

      println(s"updating ${x.id}")
      db.run(update)
    })

    Await.result(f, Duration.Inf)
  }
}
