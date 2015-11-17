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
}

case class AnagramMatch(id: Int,
                        tweet1Id: UUID,
                        tweet2Id: UUID,
                        editDistanceOriginalText: Int,
                        editDistanceStrippedText: Int,
                        posted: Boolean = false)

class AnagramMatches(tag: Tag) extends Table[AnagramMatch](tag, "ANAGRAM_MATCHES") {

  val tweets = TableQuery[Tweets]

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def tweet1Id: Rep[UUID] = column[UUID]("TWEET1_ID")
  def tweet2Id: Rep[UUID] = column[UUID]("TWEET2_ID")
  def editDistanceOriginalText = column[Int]("EDIT_DISTANCE_ORIGINAL_TEXT")
  def editDistanceStrippedText = column[Int]("EDIT_DISTANCE_STRIPPED_TEXT")
  def posted = column[Boolean]("POSTED")

  def tweet1 = foreignKey("TWEET1_FK", tweet1Id, tweets)(x => x.id,
    onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def tweet2 = foreignKey("TWEET2_FK", tweet2Id, tweets)(x => x.id,
    onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, tweet1Id, tweet2Id, editDistanceOriginalText, editDistanceStrippedText, posted) <> (AnagramMatch.tupled, AnagramMatch.unapply)
}
