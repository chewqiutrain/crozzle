-- Sets up database for crozzle

-- this guy doesn't play ball. need to do it once in db postgres, once in db crobie. or maybe just need to wait x min for install?
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE ROLE crobie WITH LOGIN SUPERUSER;

CREATE DATABASE crobie;

GRANT ALL PRIVILEGES ON DATABASE crobie TO crobie;

-- idk why this works in influx but not here...
-- work around is to do
-- \c crobie
SET search_path TO crobie;

-- this guy doesn't play ball. need to do it once in db postgres, once in db crobie. or maybe just need to wait x min for install?
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;


CREATE SCHEMA emc;

ALTER SCHEMA emc OWNER TO crobie;


SET search_path TO emc;

-- should player_id default to uuid_generate_v4() i.e. random UUID, so cannot replicate given a name.
-- use case would be to default to v4 if trigger fails. but is that a good idea?
CREATE TABLE players (
    player_id UUID NOT NULL,
    player_name VARCHAR(256) NOT NULL,
    created_timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT transaction_timestamp(),
    application_version VARCHAR(64),
    author VARCHAR(128) DEFAULT 'crobie'
);

ALTER TABLE emc.players OWNER TO crobie;

-- same issue as in emc.players, should I default to uuid_generate_v4() ?
CREATE TABLE emc.scores (
    score_id UUID NOT NULL,
    player_id UUID NOT NULL,
    score INTEGER NOT NULL,
    game_date DATE NOT NULL DEFAULT (NOW() AT TIME ZONE 'US/Eastern')::DATE,
    created_timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT transaction_timestamp(),
    application_version VARCHAR(64),
    author VARCHAR(128) DEFAULT 'crobie'
);

ALTER TABLE emc.scores OWNER TO crobie;


-- create function to generate player_id, clean up player_name
CREATE OR REPLACE FUNCTION emc.players_insert_with_id()
RETURNS TRIGGER AS $players_insert_with_id$
BEGIN
    NEW.player_name = BTRIM(LOWER(NEW.player_name));
    NEW.player_id = uuid_generate_v5(uuid_nil(), NEW.player_name);
    RETURN NEW;
END;
$players_insert_with_id$ LANGUAGE plpgsql;

ALTER FUNCTION emc.players_insert_with_id() OWNER TO crobie;

DROP TRIGGER IF EXISTS emc_players_make_id ON emc.players;
CREATE TRIGGER emc_players_make_id BEFORE INSERT ON emc.players FOR EACH ROW EXECUTE PROCEDURE emc.players_insert_with_id();

ALTER TABLE ONLY emc.players ADD CONSTRAINT emc_players_pkey PRIMARY KEY (player_id);

-- test:
-- INSERT INTO emc.players (player_name, application_version, author) VALUES ('chewbacca', '0.0.1', 'yoda');


CREATE OR REPLACE FUNCTION emc.scores_insert_with_id()
RETURNS TRIGGER AS $scores_insert_with_id$
BEGIN
   NEW.score_id = uuid_generate_v5(uuid_nil(), NEW.player_id::TEXT || NEW.score::TEXT || NEW.game_date::TEXT);
   RETURN NEW;
END;
$scores_insert_with_id$ LANGUAGE plpgsql;

ALTER FUNCTION emc.scores_insert_with_id() OWNER TO crobie;


DROP TRIGGER IF EXISTS emc_scores_make_id ON emc.scores;
CREATE TRIGGER emc_scores_make_id BEFORE INSERT ON emc.scores FOR EACH ROW EXECUTE PROCEDURE emc.scores_insert_with_id();


ALTER TABLE emc.scores ADD CONSTRAINT score_positive CHECK (score > 0);
ALTER TABLE emc.scores ADD CONSTRAINT scores_player_id_fkey FOREIGN KEY (player_id) REFERENCES emc.players(player_id);
ALTER TABLE emc.scores ADD CONSTRAINT emc_scores_pkey PRIMARY KEY (score_id);

-- test:
-- INSERT INTO emc.scores (player_id, score, game_date, application_version, author) VALUES ('2e7ceee8-258b-5a51-9222-d98e0d2788c6', 34, '2019-07-20', '0.0.1','yoda');
-- should fail due to non-positive score:
-- INSERT INTO emc.scores (player_id, score, game_date, application_version, author) VALUES ('2e7ceee8-258b-5a51-9222-d98e0d2788c6', -1, '2019-07-19', '0.0.1','yoda');
-- should fail due to FK constraint;
-- INSERT INTO emc.scores (player_id, score, game_date, application_version, author) VALUES (uuid_generate_v4(), 34, '2019-07-20', '0.0.1','yoda');

-- convenience view
CREATE VIEW emc.player_scores AS (
 SELECT p.player_id,
    s.score_id,
    p.player_name,
    s.score,
    s.game_date
    FROM emc.scores AS s
    JOIN emc.players AS p
        ON s.player_id = p.player_id
    ORDER BY p.player_name, s.game_date
);



ALTER VIEW emc.player_scores OWNER TO crobie;
