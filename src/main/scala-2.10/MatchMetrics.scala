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

  def hammingDistance(s1: String, s2: String): Int = {
    s1.zip(s2).count(c => c._1 != c._2)
  }

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
}
