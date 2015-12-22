import matching.MatchMetrics
import org.scalatest.FunSuite

class LongestCommonSubstringLengthTests extends FunSuite {

  val d: (String, String) => Int = MatchMetrics.longestCommonSubstring

  test("the same string has a substring equal to its own length") {
    assert(d("hello", "hello") == 5)
    assert(d("hello how are you", "hello how are you") == 17)
  }

  test("completely different strings") {
    assert(d("hello", "goodbye") == 1) // "o"
    assert(d("one two three", "four five six") == 2) // "e "
  }

  test("anagrams") {
    assert(d("The Game is ON", "Omg he's eatin!!!") == 2) // "s "
    assert(d("I'm used to it", "Studiotime.") == 1)
  }
}
