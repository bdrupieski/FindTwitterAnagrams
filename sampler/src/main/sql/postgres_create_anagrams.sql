-- Table: public.anagram_matches

-- DROP TABLE public.anagram_matches;

CREATE TABLE public.anagram_matches
(
  id integer NOT NULL DEFAULT nextval('anagram_matches_id_seq'::regclass),
  tweet1_id uuid NOT NULL,
  tweet2_id uuid NOT NULL,
  edit_distance_original_text integer NOT NULL,
  edit_distance_stripped_text integer NOT NULL,
  hamming_distance_stripped_text integer NOT NULL,
  longest_common_substring_length_stripped_text integer NOT NULL,
  word_count_difference integer NOT NULL,
  total_words integer NOT NULL,
  inverse_lcs_length_to_total_length_ratio real NOT NULL,
  edit_distance_to_length_ratio real NOT NULL,
  different_word_count_to_total_word_count_ratio real NOT NULL,
  is_same_rearranged integer NOT NULL,
  interesting_factor real NOT NULL,
  posted boolean NOT NULL,
  rejected boolean NOT NULL,
  CONSTRAINT anagram_matches_pkey PRIMARY KEY (id),
  CONSTRAINT anagram_matches_tweet1_id_fkey FOREIGN KEY (tweet1_id)
  REFERENCES public.tweets (id) MATCH SIMPLE
  ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT anagram_matches_tweet2_id_fkey FOREIGN KEY (tweet2_id)
  REFERENCES public.tweets (id) MATCH SIMPLE
  ON UPDATE RESTRICT ON DELETE RESTRICT
)
WITH (
OIDS=FALSE
);
ALTER TABLE public.anagram_matches
OWNER TO postgres;
