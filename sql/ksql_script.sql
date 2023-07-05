 SET 'auto.offset.reset' = 'earliest';

 CREATE STREAM pg(
        id INTEGER KEY,
        name STRING
)
WITH (
    KAFKA_TOPIC='pg-operators',
    KEY_FORMAT='AVRO',
    VALUE_FORMAT='AVRO'
);

CREATE STREAM pgfilter(
    id INTEGER KEY,
    name STRING
)
WITH (
    KAFKA_TOPIC='pg-operators-filter',
    KEY_FORMAT='AVRO',
    VALUE_FORMAT='AVRO'
);

INSERT INTO pgfilter
    SELECT * FROM pg WHERE name LIKE 'B%';