import java.sql.Timestamp
import java.util.UUID

import slick.driver.H2Driver.api._

case class Tweet(id: UUID,
                 statusId: Long,
                 createdAt: Timestamp,
                 tweetOriginalText: String,
                 tweetStrippedText: String,
                 tweetSortedStrippedText: String,
                 userId: Long,
                 userName: String,
                 isMatched: Boolean = false)

class Tweets(tag: Tag) extends Table[Tweet](tag, "TWEETS") {

  def id: Rep[UUID] = column[UUID]("ID", O.PrimaryKey)
  def statusId: Rep[Long] = column[Long]("STATUS_ID")
  def createdAt: Rep[Timestamp] = column[Timestamp]("CREATED_AT")
  def tweetOriginalText: Rep[String] = column[String]("ORIGINAL_TEXT", O.Length(200))
  def tweetStrippedText: Rep[String] = column[String]("STRIPPED_TEXT", O.Length(200))
  def tweetSortedStrippedText: Rep[String] = column[String]("STRIPPED_SORTED_TEXT", O.Length(200))
  def userId: Rep[Long] = column[Long]("USER_ID")
  def userName: Rep[String] = column[String]("USER_NAME", O.Length(50))
  def isMatched: Rep[Boolean] = column[Boolean]("IS_MATCHED")

  def * = (id, statusId, createdAt, tweetOriginalText, tweetStrippedText, tweetSortedStrippedText,
    userId, userName, isMatched) <> (Tweet.tupled, Tweet.unapply)

  def idx = index("STRIPPED_SORTED_TEXT_INDEX", tweetSortedStrippedText)
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
                        posted: Boolean = false)

class AnagramMatches(tag: Tag) extends Table[AnagramMatch](tag, "ANAGRAM_MATCHES") {

  val tweets = TableQuery[Tweets]

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def tweet1Id: Rep[UUID] = column[UUID]("TWEET1_ID")
  def tweet2Id: Rep[UUID] = column[UUID]("TWEET2_ID")
  def editDistanceOriginalText = column[Int]("EDIT_DISTANCE_ORIGINAL_TEXT")
  def editDistanceStrippedText = column[Int]("EDIT_DISTANCE_STRIPPED_TEXT")
  def hammingDistanceStrippedText = column[Int]("HAMMING_DISTANCE_STRIPPED_TEXT")
  def longestCommonSubstringLengthStrippedText = column[Int]("LONGEST_COMMON_SUBSTRING_LENGTH_STRIPPED_TEXT")
  def wordCountDifference = column[Int]("WORD_COUNT_DIFFERENCE")
  def totalWords = column[Int]("TOTAL_WORDS")
  def inverseLcsLengthToTotalLengthRatio = column[Float]("INVERSE_LCS_LENGTH_TO_TOTAL_LENGTH_RATIO")
  def editDistanceToLengthRatio = column[Float]("EDIT_DISTANCE_TO_LENGTH_RATIO")
  def differentWordCountToTotalWordCount = column[Float]("DIFFERENT_WORD_COUNT_TO_TOTAL_WORD_COUNT_RATIO")
  def isSameRearranged = column[IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum]("IS_SAME_REARRANGED")
  def interestingFactor = column[Float]("INTERESTING_FACTOR")
  def posted = column[Boolean]("POSTED")

  def tweet1 = foreignKey("TWEET1_FK", tweet1Id, tweets)(x => x.id,
    onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def tweet2 = foreignKey("TWEET2_FK", tweet2Id, tweets)(x => x.id,
    onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, tweet1Id, tweet2Id, editDistanceOriginalText, editDistanceStrippedText,
    hammingDistanceStrippedText, longestCommonSubstringLengthStrippedText, wordCountDifference, totalWords,
    inverseLcsLengthToTotalLengthRatio, editDistanceToLengthRatio, differentWordCountToTotalWordCount, isSameRearranged,
    interestingFactor, posted) <> (AnagramMatch.tupled, AnagramMatch.unapply)

  implicit val myEnumMapper = MappedColumnType.base[IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum, Int](
    e => e.id,
    {
      case 1 => IsSameWhenRearrangedEnum.TRUE
      case 0 => IsSameWhenRearrangedEnum.FALSE
      case -1 => IsSameWhenRearrangedEnum.TOO_LONG_TO_COMPUTE
    }
  )
}

