DELETE FROM
  TWEETS
WHERE
  ID IN (SELECT T.ID
         FROM
           TWEETS T
         WHERE
           T.ID NOT IN (SELECT A.TWEET1_ID
                        FROM ANAGRAM_MATCHES A) AND
           T.ID NOT IN (SELECT A.TWEET2_ID
                        FROM ANAGRAM_MATCHES A) AND
           T.STRIPPED_TEXT IN (SELECT T.STRIPPED_TEXT
                               FROM
                                 Tweets T
                               GROUP BY
                                 T.STRIPPED_TEXT
                               HAVING
                                 COUNT(*) > 1));