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
