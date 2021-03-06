SELECT
  A.LONGEST_COMMON_SUBSTRING_LENGTH_STRIPPED_TEXT                       AS LCS,
  A.EDIT_DISTANCE_STRIPPED_TEXT                                         AS EDIT,
  A.WORD_COUNT_DIFFERENCE                                               AS DIFF_WORDS,
  A.INVERSE_LCS_LENGTH_TO_TOTAL_LENGTH_RATIO                            AS LCS_RATIO,
  A.EDIT_DISTANCE_TO_LENGTH_RATIO                                       AS EDIT_RATIO,
  A.DIFFERENT_WORD_COUNT_TO_TOTAL_WORD_COUNT_RATIO                      AS COUNT_RATIO,
  A.INTERESTING_FACTOR                                                  AS INTERESTING,
  T1.ORIGINAL_TEXT                                                      AS T1_ORIG,
  T2.ORIGINAL_TEXT                                                      AS T2_ORIG,
  CONCAT('http://twitter.com/', T1.USER_NAME, '/status/', T1.STATUS_ID) AS TWEET1_URL,
  CONCAT('http://twitter.com/', T2.USER_NAME, '/status/', T2.STATUS_ID) AS TWEET2_URL
FROM
  ANAGRAM_MATCHES A
  INNER JOIN TWEETS T1 ON A.TWEET1_ID = T1.ID
  INNER JOIN TWEETS T2 ON A.TWEET2_ID = T2.ID
WHERE
  A.ID NOT IN (SELECT A.ID
               FROM
                 ANAGRAM_MATCHES A
                 INNER JOIN TWEETS T1 ON A.TWEET1_ID = T1.ID
                 INNER JOIN TWEETS T2 ON A.TWEET2_ID = T2.ID
               WHERE
                 A.DIFFERENT_WORD_COUNT_TO_TOTAL_WORD_COUNT_RATIO > 0 AND
                 A.EDIT_DISTANCE_TO_LENGTH_RATIO > 0 AND
                 A.INVERSE_LCS_LENGTH_TO_TOTAL_LENGTH_RATIO > 0 AND
                 A.IS_SAME_REARRANGED != 1)
ORDER BY
  A.INTERESTING_FACTOR DESC;