package models

import java.sql.Timestamp
import java.util.UUID

case class Tweet(id: UUID,
                 statusId: Long,
                 createdAt: Timestamp,
                 tweetOriginalText: String,
                 tweetStrippedText: String,
                 tweetSortedStrippedText: String,
                 userId: Long,
                 userName: String,
                 isMatched: Boolean = false)
