-- same issue as in emc.players, should I default to uuid_generate_v4() ?
CREATE TABLE emc.scores (
    score_id                UUID NOT NULL,
    player_id               UUID NOT NULL,
    score                   INTEGER NOT NULL,
    game_date               DATE NOT NULL DEFAULT (NOW() AT TIME ZONE 'US/Eastern')::DATE,
    created_timestamp       TIMESTAMP WITHOUT TIME ZONE DEFAULT transaction_timestamp(),
    application_version     VARCHAR(64),
    author                  VARCHAR(128) DEFAULT 'crobie'
);

ALTER TABLE emc.scores OWNER TO crobie;
