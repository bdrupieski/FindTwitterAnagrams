package tables

import java.sql.Timestamp
import java.util.UUID

import models.Tweet
import slick.driver.PostgresDriver.api._

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
    userId, userName, isMatched) <> ((Tweet.apply _).tupled, Tweet.unapply)

  def idx = index("stripped_sorted_text_index", tweetSortedStrippedText)
}
