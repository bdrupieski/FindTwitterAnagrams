import java.util.regex.Pattern

trait NormalizeSupport {
  import java.text.Normalizer.{ normalize => jnormalize, _ }

  val alphanumericRegex = Pattern.compile("[^a-z0-9]")

  def normalize(in: String): String = {
    val cleaned = jnormalize(in.trim.toLowerCase, Form.NFD)
    val transformed = cleaned.replaceAll("ß", "ss").replaceAll("ø", "o")
    alphanumericRegex.matcher(transformed).replaceAll("")
  }
}

object NormalizeSupport extends NormalizeSupport