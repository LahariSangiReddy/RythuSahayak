CREATE TABLE IF NOT EXISTS jdbc_feature_demo (
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO jdbc_feature_demo (id, label)
SELECT 1, 'Scrollable sample row'
WHERE NOT EXISTS (SELECT 1 FROM jdbc_feature_demo WHERE id = 1);

INSERT INTO jdbc_feature_demo (id, label)
SELECT 2, 'RowSet sample row'
WHERE NOT EXISTS (SELECT 1 FROM jdbc_feature_demo WHERE id = 2);
