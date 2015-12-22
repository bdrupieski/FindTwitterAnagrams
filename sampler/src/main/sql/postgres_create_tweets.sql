-- Table: public.tweets

-- DROP TABLE public.tweets;

CREATE TABLE public.tweets
(
  id uuid NOT NULL,
  status_id bigint NOT NULL,
  created_at timestamp without time zone NOT NULL,
  original_text text NOT NULL,
  stripped_text text NOT NULL,
  stripped_sorted_text text NOT NULL,
  user_id bigint NOT NULL,
  user_name text NOT NULL,
  is_matched boolean NOT NULL,
  CONSTRAINT tweets_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.tweets
  OWNER TO postgres;

-- Index: public.stripped_sorted_text_index

-- DROP INDEX public.stripped_sorted_text_index;

CREATE INDEX stripped_sorted_text_index
  ON public.tweets
  USING btree
  (stripped_sorted_text COLLATE pg_catalog."default");

