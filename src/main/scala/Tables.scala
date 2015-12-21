import java.sql.Timestamp
import java.util.UUID

import slick.driver.PostgresDriver.api._

case class Tweet(id: UUID,
                 statusId: Long,
                 createdAt: Timestamp,
                 tweetOriginalText: String,
                 tweetStrippedText: String,
                 tweetSortedStrippedText: String,
                 userId: Long,
                 userName: String,
                 isMatched: Boolean = false)

class Tweets(tag: Tag) extends Table[Tweet](tag, Some("public"), "tweets") {

  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def statusId: Rep[Long] = column[Long]("status_id")
  def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")
  def tweetOriginalText: Rep[String] = column[String]("original_text")
  def tweetStrippedText: Rep[String] = column[String]("stripped_text")
  def tweetSortedStrippedText: Rep[String] = column[String]("stripped_sorted_text")
  def userId: Rep[Long] = column[Long]("user_id")
  def userName: Rep[String] = column[String]("user_name")
  def isMatched: Rep[Boolean] = column[Boolean]("is_matched")

  def * = (id, statusId, createdAt, tweetOriginalText, tweetStrippedText, tweetSortedStrippedText,
    userId, userName, isMatched) <> (Tweet.tupled, Tweet.unapply)

  def idx = index("stripped_sorted_text_index", tweetSortedStrippedText)
}

case class AnagramMatch(id: Int,
                        tweet1Id: UUID,
                        tweet2Id: UUID,
                        editDistanceOriginalText: Int,
                        editDistanceStrippedText: Int,
                        hammingDistanceStrippedText: Int,
                        longestCommonSubstringLengthStrippedText: Int,
                        wordCountDifference: Int,
                        totalUniqueWords: Int,
                        lcsLengthToTotalLengthRatio: Float,
                        editDistanceToLengthRatio: Float,
                        differentWordCountToTotalWordCount: Float,
                        isSameRearranged: IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum,
                        interestingFactor: Float,
                        posted: Boolean = false,
                        rejected: Boolean = false)

class AnagramMatches(tag: Tag) extends Table[AnagramMatch](tag, Some("public"), "anagram_matches") {
  val tweets = TableQuery[Tweets]

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def tweet1Id: Rep[UUID] = column[UUID]("tweet1_id")
  def tweet2Id: Rep[UUID] = column[UUID]("tweet2_id")
  def editDistanceOriginalText = column[Int]("edit_distance_original_text")
  def editDistanceStrippedText = column[Int]("edit_distance_stripped_text")
  def hammingDistanceStrippedText = column[Int]("hamming_distance_stripped_text")
  def longestCommonSubstringLengthStrippedText = column[Int]("longest_common_substring_length_stripped_text")
  def wordCountDifference = column[Int]("word_count_difference")
  def totalWords = column[Int]("total_words")
  def inverseLcsLengthToTotalLengthRatio = column[Float]("inverse_lcs_length_to_total_length_ratio")
  def editDistanceToLengthRatio = column[Float]("edit_distance_to_length_ratio")
  def differentWordCountToTotalWordCount = column[Float]("different_word_count_to_total_word_count_ratio")
  def isSameRearranged = column[IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum]("is_same_rearranged")
  def interestingFactor = column[Float]("interesting_factor")
  def posted = column[Boolean]("posted")
  def rejected = column[Boolean]("rejected")

  def tweet1 = foreignKey("anagram_matches_tweet1_id_fkey", tweet1Id, tweets)(x => x.id,
    onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def tweet2 = foreignKey("anagram_matches_tweet2_id_fkey", tweet2Id, tweets)(x => x.id,
    onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, tweet1Id, tweet2Id, editDistanceOriginalText, editDistanceStrippedText,
    hammingDistanceStrippedText, longestCommonSubstringLengthStrippedText, wordCountDifference, totalWords,
    inverseLcsLengthToTotalLengthRatio, editDistanceToLengthRatio, differentWordCountToTotalWordCount, isSameRearranged,
    interestingFactor, posted, rejected) <> (AnagramMatch.tupled, AnagramMatch.unapply)

  implicit val myEnumMapper = MappedColumnType.base[IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum, Int](
    e => e.id,
    {
      case 1 => IsSameWhenRearrangedEnum.TRUE
      case 0 => IsSameWhenRearrangedEnum.FALSE
      case -1 => IsSameWhenRearrangedEnum.TOO_LONG_TO_COMPUTE
    }
  )
}
