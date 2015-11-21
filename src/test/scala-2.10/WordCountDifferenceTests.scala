import org.scalatest.FunSuite

class WordCountDifferenceTests extends FunSuite {

  val diff: (String, String) => (Int, Int) = TweetFilters.getWordCountDifference

  test("same words different order") {
    assert(diff("this is a test", "this is a test") == (0, 8))
    assert(diff("this is a test", "is this a test?") == (0, 8))
    assert(diff("this is a test", "a test, this is") == (0, 8))
    assert(diff("this is a test", "this, a test, is") == (0, 8))
    assert(diff("this is a test", "$THIS IS A TEST!!!!") == (0, 8))
  }

  test("1 additional word") {
    assert(diff("this is a test", "this is a test too") == (1, 9))
    assert(diff("this is a test", "this is also a test") == (1, 9))
    assert(diff("this is a test", "yes this is a test") == (1, 9))
  }

  test("multiple additional words") {
    assert(diff("this is a test", "this is a test, indeed it is") == (3, 11))
    assert(diff("this is a test", "this is a test of the emergency broadcast system") == (5, 13))
  }

  test("different words in each, some overlap") {
    assert(diff("one two three", "two three four") == (2, 6))
    assert(diff("two three four", "three four five six") == (3, 7))
  }

  test("completely different phrases, no overlap") {
    assert(diff("one two three", "four five six") == (6, 6))
    assert(diff("to be or not to be", "indeed") == (7, 7))
    assert(diff("yes yes yes yes yes", "no no no no no") == (10, 10))
    assert(diff("yes yes yes yes yes", "nononono non nonono nono on") == (10, 10))
  }

  test("don't count whitespace") {
    assert(diff("        yes       ", "      no            ") == (2, 2))
    assert(diff("yes yes yes", "   no no no") == (6, 6))
    assert(diff("   yes   yes   yes   ", "   no   no   no   ") == (6, 6))
    assert(diff("yes\nyes\nyes\n", "\r\nno\nno\t\tno") == (6, 6))
    assert(diff("yes\n\t\r\nyes\nyes\n", " \r\nyes\t\r\n\n\r  \nyes\t \tyes") == (0, 6))
  }

  test("non-characters get replaced with spaces") {
    assert(diff("this.is.a.test", "this is a test") == (0, 8))
    assert(diff("!!!~~this.is\t\r\r\r\n\n\na.test%~ ~ ~ ~ ~", ")!@$)&*^!@$&*this.~^()is%a    ! !! !~ test%%") == (0, 8))
    assert(diff("Why I can't sleep _-_!@#$%^&*()_+-=~`/", "Why cant i sleep") == (0, 8))
  }
}
