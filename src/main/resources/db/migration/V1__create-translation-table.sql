CREATE TABLE IF NOT EXISTS translation (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_ip inet NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    source_language TEXT NOT NULL,
    target_language TEXT NOT NULL,
    text TEXT NOT NULL,
    translated_text TEXT NOT NULL
);
