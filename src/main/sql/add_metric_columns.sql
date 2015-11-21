ALTER TABLE PUBLIC.ANAGRAM_MATCHES
ADD HAMMING_DISTANCE_STRIPPED_TEXT integer DEFAULT 0 NOT NULL
AFTER EDIT_DISTANCE_STRIPPED_TEXT;

ALTER TABLE PUBLIC.ANAGRAM_MATCHES
ADD LONGEST_COMMON_SUBSTRING_LENGTH_STRIPPED_TEXT integer DEFAULT 0 NOT NULL
AFTER HAMMING_DISTANCE_STRIPPED_TEXT;

ALTER TABLE PUBLIC.ANAGRAM_MATCHES
ADD WORD_COUNT_DIFFERENCE integer DEFAULT 0 NOT NULL
AFTER LONGEST_COMMON_SUBSTRING_LENGTH_STRIPPED_TEXT;
