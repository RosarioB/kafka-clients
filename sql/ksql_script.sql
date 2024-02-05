SET 'auto.offset.reset' = 'earliest';

CREATE STREAM operators(
        id INTEGER KEY,
        company_name STRING,
        nationality STRING,
        capitalization BIGINT
)
WITH (
    KAFKA_TOPIC='pg-operators',
    KEY_FORMAT='AVRO',
    VALUE_FORMAT='AVRO'
);
