import org.scalatest.FunSuite

class NormalizeStringTests extends FunSuite with NormalizeSupport {

  test("normalize non-ASCII characters") {
    assert(normalize("ÀÁÂÃĀĂȦÄẢÅǍȀȂĄẠḀẦẤàáâä") == "aaaaaaaaaaaaaaaaaaaaaa")
    assert(normalize("ÉÊẼĒĔËȆȄȨĖèéêẽēȅë") == "eeeeeeeeeeeeeeeee")
    assert(normalize("ÌÍÏïØøÒÖÔöÜüŇñÇçß") == "iiiioooooouunnccss")
  }

  test("lowercase") {
    assert(normalize("ABCDabcd") == "abcdabcd")
  }

  test("spaces") {
    assert(normalize("This is a test!") == "thisisatest")
  }
}
