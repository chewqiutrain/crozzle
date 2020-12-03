-- should player_id default to uuid_generate_v4() i.e. random UUID, so cannot replicate given a name.
-- use case would be to default to v4 if trigger fails. but is that a good idea?
CREATE TABLE emc.players (
    player_id               UUID NOT NULL,
    player_name             VARCHAR(256) NOT NULL,
    created_timestamp       TIMESTAMP WITHOUT TIME ZONE DEFAULT transaction_timestamp(),
    application_version     VARCHAR(64),
    author                  VARCHAR(128) DEFAULT 'crobie'
);

ALTER TABLE emc.players OWNER TO crobie;