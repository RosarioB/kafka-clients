# ksql
In the branch `kafka-connect` we imported the data from a postgres database into a Kafka topic by making use 
of a source connector. In this branch we will add two more steps to it:
- we will use ksql to perform a simple filtering on the data
- then we will upload the filtered data to a mysql database using a sink connector. 

This project contains three folders:
- docker: which contains the docker compose file.
- jars: which contains the jars of the [JDBC Connector](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc/). I have also added the jdbc driver for MySQL (mysql-connector-j-8.0.32.jar)
- sql: which contains now three script: mysql_script.sql, ksql_script.sql, script_postgres.sql

To run the project we need to
1. run the docker compose file
2. set up the postgres database
3. set up the mysql database
4. configure the Kafka source connector to Postgres
5. set up the ksql streams
6. configure the Kafka sink connector to MySQL

Please follow the following steps in the exact same order to make sure that everything will work fine.

## 1. Docker
To start the file kafka-docker-compose.yml we need to get inside the docker directory and run: `docker-compose -f kafka-docker-compose.yml up -d`

## 2. Postgres
To enter the postgres cli we can execute `docker exec -it postgres psql -U postgres`

In the folder sql there is a file script.sql which creates and populates the `operators` table. 

To run this file we can execute: `\i sql/script_postgres.sql`

## 3. MySQL
To connect to MySQL cli we need to execute

`docker exec -it mysql mysql -u root -p` then we can enter `password` as password.

Then we can execute the script `mysql_script.sql`:

`source sql/mysql_script.sql;` to create the table `filtered_operators`.

## 4. Create the topics
We need to create the topic `pg-operators`. To do that we have to get inside the broker container by executing:
`docker exec -it broker bash`

Then we create the topic by executing:
```
kafka-topics \
    --bootstrap-server broker:29092 \
    --create \
    --topic pg-operators \
    --partitions 1 \
    --replication-factor 1
```

Then we need the topic `pg-operators-filter`

```
kafka-topics \
    --bootstrap-server broker:29092 \
    --create \
    --topic pg-operators-filter \
    --partitions 1 \
    --replication-factor 1    
```
## 5. Create the source connector

To add the JDBC source connector which allows Kafka Connect to import the data from our Postgres database into the pg-operators topic
we need to make a POST request to the Kafka Connect REST API.

First we need to get inside the connect container with: `docker exec -it connect bash`

```
curl -s -X POST \
        -H "Content-Type: application/json" \
        --data '{
            "name": "Source-Connector",
            "config": {
                "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
                "connection.url": "jdbc:postgresql://postgres:5432/postgres",
                "connection.user": "postgres",
                "connection.password": "password",
                "table.whitelist": "operators",
                "mode":"incrementing",
                "incrementing.column.name": "id",
                "table.types": "TABLE",
                "topic.prefix": "pg-",
                "numeric.mapping": "best_fit",
                "transforms": "createKey,extractInt",
                "transforms.createKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
                "transforms.createKey.fields": "id",
                "transforms.extractInt.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
                "transforms.extractInt.field": "id"
            }
        }' http://connect:8083/connectors
```

## 6. KSQL
To enter the ksql cli we can run: `docker exec -it ksqldb-server ksql http://ksqldb-server:8088`

Now we can execute the script that creates the streams `pg` from the topic `pg-operators` and `pgfilter` from the topic `pg-operators-filter`, 
by executing `RUN SCRIPT '/sql/ksql_script.sql';`

The topic `pg-operators-filter` is populated with the records from the topic `pg-operators` filtered by the name starting with B, by using a query `INSERT INTO`.

At this time in the ksql shell if you run `select * from pg;` it should print:

```
+--------------------------------------+--------------------------------------+
|ID                                    |NAME                                  |
+--------------------------------------+--------------------------------------+
|6                                     |Oy Pohjolan Liikenne Ab               |
|12                                    |Helsingin Bussiliikenne Oy            |
|17                                    |Tammelundin Liikenne Oy               |
|18                                    |Pohjolan Kaupunkiliikenne Oy          |
|19                                    |Etelä-Suomen Linjaliikenne Oy         |
|20                                    |Bus Travel Åbergin Linja Oy           |
|21                                    |Bus Travel Oy Reissu Ruoti            |
|22                                    |Nobina Finland Oy                     |
|36                                    |Nurmijärven Linja Oy                  |
|40                                    |HKL-Raitioliikenne                    |
|45                                    |Transdev Vantaa Oy                    |
|47                                    |Taksikuljetus Oy                      |
|51                                    |Korsisaari Oy                         |
|54                                    |V-S Bussipalvelut Oy                  |
|55                                    |Transdev Helsinki Oy                  |
|58                                    |Koillisen Liikennepalvelut Oy         |
|59                                    |Tilausliikenne Nikkanen Oy            |
|90                                    |VR Oy                                 |
+--------------------------------------+--------------------------------------+
```

And then if you run `select * from pgfilter;` it should print:

```
+--------------------------------------+--------------------------------------+
|ID                                    |NAME                                  |
+--------------------------------------+--------------------------------------+
|20                                    |Bus Travel Åbergin Linja Oy           |
|21                                    |Bus Travel Oy Reissu Ruoti            |
+--------------------------------------+--------------------------------------+
```

## 7. Create the sink connector
As we did in the source connector we need to get to the bash shell of the `connect` container with: `docker exec -it connect bash`

Then we need to run this CURL:

```
curl -s -X POST \
        -H "Content-Type: application/json" \
        --data '{
            "name": "Sink-Connector3",
            "config": {
                "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
                "connection.url": "jdbc:mysql://mysql:3306/mysql",
                "connection.user": "root",
                "connection.password": "password",
                "dialect.name": "MySqlDatabaseDialect",
                "topics":"pg-operators-filter",
                "group.id": "sink.group",
                "key.converter": "io.confluent.connect.avro.AvroConverter",
                "key.converter.schema.registry.url": "http://schema-registry:8081",
                "value.converter": "io.confluent.connect.avro.AvroConverter",
                "value.converter.schema.registry.url": "http://schema-registry:8081",
                "table.name.format": "filtered_operators",
                "auto.create": "true",
                "insert.mode": "upsert",
                "pk.fields": "id",
                "pk.mode": "record_key"
            }
        }' http://connect:8083/connectors
```
## 8. Test the results

To test that everything works we can connect to the mysql cli and then execute:
```
USE mysql;
SELECT * FROM filtered_operators;
```

The result should be:
```
+----+-----------------------------+
| id | name                        |
+----+-----------------------------+
| 20 | Bus Travel ?bergin Linja Oy |
| 21 | Bus Travel Oy Reissu Ruoti  |
+----+-----------------------------+
```

To check if the upsert is working correctly we can connect to the ksql cli:
`docker exec -it ksqldb-server ksql http://ksqldb-server:8088`:

and insert a new value for the key 21 like this:
`insert into pgfilter (id, name) values(21, 'Bus Travel by Rosario');`

And now in the mysql container if we run: `SELECT * FROM filtered_operators;`
we should find:

```
+----+-----------------------------+
| id | name                        |
+----+-----------------------------+
| 20 | Bus Travel ?bergin Linja Oy |
| 21 | Bus Travel by Rosario       |
+----+-----------------------------+
```

So the key 21 has been correctly updated.

## Notes
To check in detail the Kafka cluster you can either use [Kadeck](https://www.kadeck.com/) or the Confluent Control Center at http://localhost:9021.
