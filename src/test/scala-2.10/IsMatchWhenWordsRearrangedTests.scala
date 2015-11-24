import MatchMetrics.IsSameWhenRearrangedEnum
import MatchMetrics.IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum
import org.scalatest.FunSuite

class IsMatchWhenWordsRearrangedTests extends FunSuite {

  val isMatch: (String, String) => IsSameWhenRearrangedEnum = MatchMetrics.isMatchWhenWordsRearranged

  test("simple matching") {
    assert(isMatch("this is sparta", "thisis sparta") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is sparta", "thisissparta") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is sparta", "sparta this is") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is sparta", "spartathisis") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is sparta", "spartaisthis") == IsSameWhenRearrangedEnum.TRUE)
    assert(isMatch("this is sparta", "THIS... IS... SPARTA!!!") == IsSameWhenRearrangedEnum.TRUE)
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
