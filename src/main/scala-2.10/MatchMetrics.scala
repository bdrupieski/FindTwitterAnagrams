import MatchMetrics.IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum

object MatchMetrics {
  // Adapted from https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance
  def demerauLevenshteinDistance(a: String, b: String): Int = {

    val aUpper = a.toUpperCase
    val bUpper = b.toUpperCase

    val aLength = aUpper.length
    val bLength = bUpper.length

    val matrix: Array[Array[Int]] = Array.ofDim[Int](aLength + 1, bLength + 1)

    for (i <- 0 to aLength) {
      matrix(i)(0) = i
    }

    for (j <- 0 to bLength) {
      matrix(0)(j) = j
    }

    for (i <- 1 to aLength; j <- 1 to bLength) {
      val cost: Int = if (bUpper(j - 1) == aUpper(i - 1)) 0 else 1

      matrix(i)(j) = Math.min(matrix(i - 1)(j    ) + 1,     // deletion
                     Math.min(matrix(i    )(j - 1) + 1,     // insertion
                              matrix(i - 1)(j - 1) + cost)) // substitution

      if (i > 1 && j > 1 && aUpper(i - 1) == bUpper(j - 2) && aUpper(i - 2) == bUpper(j - 1)) {
        matrix(i)(j) = Math.min(matrix(i)(j),
                                matrix(i - 2)(j - 2) + cost) // transposition
      }
    }

    matrix(aLength)(bLength)
  }

  // Copied from http://www.tautvidas.com/blog/2013/07/compute-hamming-distance-of-byte-arrays/
  def hammingDistance(s1: String, s2: String): Int = {
    s1.zip(s2).count(c => c._1 != c._2)
  }

  // Adapted from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Longest_common_substring#C.23
  def longestCommonSubstring(s1: String, s2: String): Int = {

    val matrix: Array[Array[Int]] = Array.ofDim[Int](s1.length, s2.length)
    var maxLength = 0

    for (i <- 0 until s1.length; j <- 0 until s2.length) {
      if (!s1(i).equals(s2(j))) {
        matrix(i)(j) = 0
      } else {
        if (i == 0 || j == 0) {
          matrix(i)(j) = 1
        } else {
          matrix(i)(j) = 1 + matrix(i - 1)(j - 1)
        }

        if (matrix(i)(j) > maxLength) {
          maxLength = matrix(i)(j)
        }
      }
    }
    maxLength
  }

  // TODO: look into more robust tokenization
  private def tokenizeTweetText(originalText: String): Array[String] = {
    val formattedText = originalText.toLowerCase.replaceAll("'", "").replaceAll("[^a-z0-9 ]+", " ")

    val words = formattedText.trim.split("\\s+")

    words
  }

  def getWordCountDifference(tweet1OriginalText: String, tweet2OriginalText: String): (Int, Int) = {

    def getWordCount(tweetOriginalText: String): Map[String, Int] = {
      tokenizeTweetText(tweetOriginalText).groupBy(x => x).map(x => (x._1, x._2.length))
    }

    val tweet1Counts = getWordCount(tweet1OriginalText)
    val tweet2Counts = getWordCount(tweet2OriginalText)

    val allWordsInBothTweets = tweet1Counts.keySet ++ tweet2Counts.keySet

    var wordDifferenceCount = 0

    for (word <- allWordsInBothTweets) {
      val countInTweet1 = tweet1Counts.getOrElse(word, 0)
      val countInTweet2 = tweet2Counts.getOrElse(word, 0)

      wordDifferenceCount += math.abs(countInTweet1 - countInTweet2)
    }

    val totalWords = tweet1Counts.values.sum + tweet2Counts.values.sum

    (wordDifferenceCount, totalWords)
  }

  object IsSameWhenRearrangedEnum extends Enumeration {
    type IsSameWhenRearrangedEnum = Value
    val TRUE = Value(1)
    val FALSE = Value(0)
    val TOO_LONG_TO_COMPUTE = Value(-1)
  }

  def isMatchWhenWordsRearranged(s1: String, s2: String): IsSameWhenRearrangedEnum = {

    val s1Tokens = tokenizeTweetText(s1)
    val s2Tokens = tokenizeTweetText(s2)

    // all permutations of 6 elements is 720 items.
    // 7 elements is 5,040. That's too much.
    if (s1Tokens.length >= 7 || s2Tokens.length >= 7) {
      IsSameWhenRearrangedEnum.TOO_LONG_TO_COMPUTE
    } else {
      val s1Perms = s1Tokens.permutations.map(x => x.mkString).toArray
      val s2Perms = s2Tokens.permutations.map(x => x.mkString).toArray

      if (s1Perms.intersect(s2Perms).nonEmpty) {
        IsSameWhenRearrangedEnum.TRUE
      } else {
        IsSameWhenRearrangedEnum.FALSE
      }
    }
  }
}
