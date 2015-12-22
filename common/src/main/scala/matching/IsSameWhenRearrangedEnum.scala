package matching

object IsSameWhenRearrangedEnum extends Enumeration {
  type IsSameWhenRearrangedEnum = Value
  val TRUE = Value(1)
  val FALSE = Value(0)
  val TOO_LONG_TO_COMPUTE = Value(-1)
}