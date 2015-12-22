import matching.IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum
import matching.{IsSameWhenRearrangedEnum, MatchMetrics}
import org.scalatest.FunSuite

class IsMatchWhenWordsRearrangedTests extends FunSuite {

  val isMatch: (String, String) => IsSameWhenRearrangedEnum = MatchMetrics.isMatchWhenWordsRearranged

  test("simple matching") {
    assert(isMatch("this is a test", "thisis a test") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is a test", "thisisatest") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is a test", "a test this is") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is a test", "atestthisis") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is a test", "testathisis") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is a test", "THIS... IS... A TEST!!!!") == IsSameWhenRearrangedEnum.TRUE)
  }

  test("real tweets") {
    assert(isMatch("heartbroken", "broken heart") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("Heartbroken \uD83D\uDE1E\uD83D\uDC94", "broken heart") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("Me is happy", "Happyisme") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("☺☺☺☺☺AllYouNeedIsLove☺☺☺☺☺", "Love is all you need.") == IsSameWhenRearrangedEnum.TRUE)
  }

  test("not a match") {
    assert(isMatch("twd is on yes", "ITS SO WENDY") == IsSameWhenRearrangedEnum.FALSE)
    assert(isMatch("Weather is so cute ❤️⛄️", "<Eraticus> oh sweet") == IsSameWhenRearrangedEnum.FALSE)
    assert(isMatch("Hungriest.", "Sure thing \uD83D\uDE0F") == IsSameWhenRearrangedEnum.FALSE)
    assert(isMatch("hostia, y med'an?", "I hate Mondays \uD83D\uDD95") == IsSameWhenRearrangedEnum.FALSE)
    assert(isMatch("The Game is ON", "Omg he's eatin!!!") == IsSameWhenRearrangedEnum.FALSE)
  }

  test("don't calculate permutations for long strings") {
    assert(isMatch("yes this is a very long string", "yes this is a very long string") == IsSameWhenRearrangedEnum.TOO_LONG_TO_COMPUTE)
  }
}
