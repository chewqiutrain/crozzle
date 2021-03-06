-- Sets up database for crozzle

-- Should be using database = crobie
-- \c crobie

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


--SET search_path TO emc;



/*
I think manage ID generation and data integrity on app side
*/

-- create function to generate player_id, clean up player_name
--CREATE OR REPLACE FUNCTION emc.players_insert_with_id()
--RETURNS TRIGGER AS $players_insert_with_id$
--BEGIN
--    NEW.player_name = BTRIM(LOWER(NEW.player_name));
--    NEW.player_id = uuid_generate_v5(uuid_nil(), NEW.player_name);
--    RETURN NEW;
--END;
--$players_insert_with_id$ LANGUAGE plpgsql;
--
--ALTER FUNCTION emc.players_insert_with_id() OWNER TO crobie;
--
--DROP TRIGGER IF EXISTS emc_players_make_id ON emc.players;
--CREATE TRIGGER emc_players_make_id BEFORE INSERT ON emc.players FOR EACH ROW EXECUTE PROCEDURE emc.players_insert_with_id();
--
--ALTER TABLE ONLY emc.players ADD CONSTRAINT emc_players_pkey PRIMARY KEY (player_id);

-- test:
-- INSERT INTO emc.players (player_name, application_version, author) VALUES ('chewbacca', '0.0.1', 'yoda');


--CREATE OR REPLACE FUNCTION emc.scores_insert_with_id()
--RETURNS TRIGGER AS $scores_insert_with_id$
--BEGIN
--   NEW.score_id = uuid_generate_v5(uuid_nil(), NEW.player_id::TEXT || NEW.score::TEXT || NEW.game_date::TEXT);
--   RETURN NEW;
--END;
--$scores_insert_with_id$ LANGUAGE plpgsql;
--
--ALTER FUNCTION emc.scores_insert_with_id() OWNER TO crobie;
--
--
--DROP TRIGGER IF EXISTS emc_scores_make_id ON emc.scores;
--CREATE TRIGGER emc_scores_make_id BEFORE INSERT ON emc.scores FOR EACH ROW EXECUTE PROCEDURE emc.scores_insert_with_id();
--
--
--ALTER TABLE emc.scores ADD CONSTRAINT score_positive CHECK (score > 0);
--ALTER TABLE emc.scores ADD CONSTRAINT uniq_player_game_date_score UNIQUE (player_id, game_date, score);
--ALTER TABLE emc.scores ADD CONSTRAINT scores_player_id_fkey FOREIGN KEY (player_id) REFERENCES emc.players(player_id);
--ALTER TABLE emc.scores ADD CONSTRAINT emc_scores_pkey PRIMARY KEY (score_id);

-- test:
-- INSERT INTO emc.scores (player_id, score, game_date, application_version, author) VALUES ('2e7ceee8-258b-5a51-9222-d98e0d2788c6', 34, '2019-07-20', '0.0.1','yoda');
-- should fail due to non-positive score:
-- INSERT INTO emc.scores (player_id, score, game_date, application_version, author) VALUES ('2e7ceee8-258b-5a51-9222-d98e0d2788c6', -1, '2019-07-19', '0.0.1','yoda');
-- should fail due to FK constraint;
-- INSERT INTO emc.scores (player_id, score, game_date, application_version, author) VALUES (uuid_generate_v4(), 34, '2019-07-20', '0.0.1','yoda');

