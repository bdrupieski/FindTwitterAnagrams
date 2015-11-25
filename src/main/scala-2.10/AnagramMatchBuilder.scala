import MatchMetrics._

object AnagramMatchBuilder {

  def buildAnagramMatch(tweet1: Tweet, tweet2: Tweet): AnagramMatch = {
    val originalTextEditDistance = demerauLevenshteinDistance(tweet1.tweetOriginalText, tweet2.tweetOriginalText)
    val strippedTextEditDistance = demerauLevenshteinDistance(tweet1.tweetStrippedText, tweet2.tweetStrippedText)
    val hammingDistanceStrippedText = hammingDistance(tweet1.tweetStrippedText, tweet2.tweetStrippedText)
    val lcsLengthStrippedText = longestCommonSubstring(tweet1.tweetStrippedText, tweet2.tweetStrippedText)
    val (wordCountDifference, totalWords) = getWordCountDifference(tweet1.tweetOriginalText, tweet2.tweetOriginalText)
    val isSameRearranged = isMatchWhenWordsRearranged(tweet1.tweetStrippedText, tweet2.tweetStrippedText)

    val length = tweet1.tweetSortedStrippedText.length
    val inverseLcsLengthToLengthRatio = 1 - (lcsLengthStrippedText.toFloat / length)
    val editDistanceToLengthRatio = strippedTextEditDistance.toFloat / length
    val diffWordCountToTotalWordCountRatio = wordCountDifference.toFloat / totalWords
    val interestingFactor = (inverseLcsLengthToLengthRatio + editDistanceToLengthRatio + diffWordCountToTotalWordCountRatio) / 3.toFloat

    AnagramMatch(0, tweet1.id, tweet2.id, originalTextEditDistance, strippedTextEditDistance,
      hammingDistanceStrippedText, lcsLengthStrippedText, wordCountDifference, totalWords,
      inverseLcsLengthToLengthRatio, editDistanceToLengthRatio, diffWordCountToTotalWordCountRatio, isSameRearranged,
      interestingFactor)
  }
}
