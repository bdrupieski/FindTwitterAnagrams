import matching.MatchMetrics
import org.scalatest.FunSuite

class DemerauLevenshteinTests extends FunSuite {

  val d: (String, String) => Int = MatchMetrics.demerauLevenshteinDistance

  test("same string has a distance of 0") {
    assert(d("", "") == 0)
    assert(d("b", "b") == 0)
    assert(d("brian", "brian") == 0)
    assert(d("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ") == 0)
  }

  test("case doesn't matter") {
    assert(d("abc", "acb") == 1)
    assert(d("ABC", "acb") == 1)
    assert(d("abc", "ACB") == 1)
  }

  test("single transpositions should have a distance of 1") {
    assert(d("ABC", "ACB") == 1)
    assert(d("brian", "brina") == 1)
    assert(d("brian", "rbian") == 1)
  }

  test("single insertions and deletions have a distance of 1") {
    assert(d("ABC", "ABCD") == 1)
    assert(d("ABCD", "ABC") == 1)

    assert(d("AABC", "ABC") == 1)
    assert(d("ABC", "AABC") == 1)

    assert(d("whoops", "whoops1") == 1)
    assert(d("whoops1", "whoops") == 1)
    assert(d("1whoops", "whoops") == 1)
    assert(d("whoops", "1whoops") == 1)
    assert(d("who1ops", "whoops") == 1)
    assert(d("whoops", "who1ops") == 1)
  }

  test("multiple insertions, deletions, and transpositions") {
    assert(d("this just in", "thisjustin") == 2)
    assert(d("thisjustin", "this just in") == 2)
    assert(d("this just in", "XXthisjustinXX") == 6)
    assert(d("XXthisjustinXX", "this just in") == 6)

    assert(d("the world is round", "th3 w0rld 1s r0und!!") == 6)
    assert(d("th3 w0rld 1s r0und!!", "the world is round") == 6)

    assert(d("12345", "2134") == 2)
    assert(d("2134", "12345") == 2)

    assert(d("12345", "54321") == 4)
    assert(d("54321", "12345") == 4)
  }

  test("very different") {
    assert(d("whoa", "HEYO") == 4)
    assert(d("ABCDEFGHI", "123456789") == 9)
    assert(d("", "123456789") == 9)
    assert(d("ABCDEFGHI", "") == 9)
  }
}