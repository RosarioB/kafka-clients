# ksql
In this branch we will use Kafka Connect to create a JDBC Source connector that acts as a producer importing into Kafka the data previously uploaded on a Postgres database. Then we will use KSQL to demostrate how it can be used to create streaming ETL pipelines.

# Project structure
This project contains three folders:
- docker: which contains the docker compose file.
- jars: which contains the jars of the [JDBC Connector](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc/).
- sql: which contains two script: ksql_script.sql, script_postgres.sql

In the following we will:
1. Run the docker compose file
2. Set up the Postgres database
3. Create the necessary topic in the Kafka cluster
4. Set up the JDBC source connector
5. Use KSQL

Please follow the following steps in the exact same order to make sure that everything will work fine.

## 1. Docker
To start the file kafka-docker-compose.yml we need to get inside the docker directory and run: `docker-compose -f kafka-docker-compose.yml up -d`

## 2. Postgres
To enter the postgres cli we can execute `docker exec -it postgres psql -U postgres`

In the folder sql there is a file script.sql which creates and populates the `operators` table. 

To run this file we can execute: `\i sql/script_postgres.sql`

## 3. Create the topics
We need to create the topic `pg-operators`. To do that we have to get inside the broker container by executing:
`docker exec -it broker bash`

Then we create the topic by executing:
```
kafka-topics --bootstrap-server broker:29092 --create --topic pg-operators --partitions 1 --replication-factor 1
```
## 4. Set up the JDBC source connector
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
### Notes on connector configuration
- Kafka messages are key/value pairs. For a JDBC connector, the value (payload) is the contents of the table row being ingested. However, the JDBC connector does not generate the key by default. To set a message key for the JDBC connector, you use two Single Message Transformations (SMTs): the ValueToKey SMT and the ExtractField SMT. For this reason we have written these 5 lines in the configuration:

    ```
    "transforms": "createKey,extractInt",
    "transforms.createKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields": "id",
    "transforms.extractInt.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractInt.field": "id"
    ```

    [JDBC Connector documentation](https://docs.confluent.io/kafka-connectors/jdbc/current/source-connector/overview.html#:~:text=Message%20keys,-Kafka%20messages%20are)

- The numeric mapping configuration has been set this way: ```"numeric.mapping": "best_fit"``` , check the [JDBC Connector documentation](https://docs.confluent.io/kafka-connectors/jdbc/current/source-connector/overview.html#:~:text=Numeric%20mapping%20property) for details.

### Check the data with kafka-avro-console-consumer
To enter into the shell of the schema registry container we can execute: ```docker exec -it schema-registry bash``` and then we can start a consumer with:

```
kafka-avro-console-consumer --bootstrap-server broker:29092 --from-beginning --topic pg-operators  --property print.key=true --property schema.registry.url=http://schema-registry:8081
```
You should see something like this:

```
1	{"id":1,"company_name":{"string":"Lufthansa"},"nationality":{"string":"German"},"capitalization":{"int":250000000}}
2	{"id":2,"company_name":{"string":"Air France"},"nationality":{"string":"French"},"capitalization":{"int":300000000}}
3	{"id":3,"company_name":{"string":"British Airways"},"nationality":{"string":"British"},"capitalization":{"int":280000000}}
...
```

## 5. KSQL
To open the ksql CLI we can run: `docker exec -it ksqldb-server ksql http://ksqldb-server:8088`

Now we can execute the script that creates the stream `operators` from the topic `pg-operators`, by executing `RUN SCRIPT '/sql/ksql_script.sql';`

## 6. KSQL exercises
### SELECT (Pull query)
A pull query returns the current state to the client, and then terminate. In that sense, they are much more akin to a SELECT statement executed on a regular RDBMS.
To check that the stream was created correctly, we can open the ksql CLI and run `SELECT * FROM operators;`:

```
+-------------------------------------+-------------------------------------+-------------------------------------+-------------------------------------+
|ID                                   |COMPANY_NAME                         |NATIONALITY                          |CAPITALIZATION                       |
+-------------------------------------+-------------------------------------+-------------------------------------+-------------------------------------+
|1                                    |Lufthansa                            |German                               |250000000                            |
|2                                    |Air France                           |French                               |300000000                            |
|3                                    |British Airways                      |British                              |280000000                            |
...
+-------------------------------------+-------------------------------------+-------------------------------------+-------------------------------------+
```
We should see all 83 rows.

Being a pull query, it auto terminates.

We can also apply filters on the queries in the WHERE clause like a normal relational query but we cannot perform aggregations using the GROPUP BY clause:

For example we can execute:

`SELECT * FROM operators WHERE nationality = 'American';`

and the result would be:

```
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|ID                                    |COMPANY_NAME                          |NATIONALITY                           |CAPITALIZATION                        |
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|11                                    |American Airlines                     |American                              |500000000                             |
|12                                    |Delta Air Lines                       |American                              |550000000                             |
|13                                    |United Airlines                       |American                              |520000000                             |
|14                                    |Southwest Airlines                    |American                              |480000000                             |
|15                                    |JetBlue Airways                       |American                              |450000000                             |
|16                                    |Amtrak                                |American                              |420000000                             |
|17                                    |Alaska Airlines                       |American                              |430000000                             |
|18                                    |Spirit Airlines                       |American                              |410000000                             |
|19                                    |Frontier Airlines                     |American                              |390000000                             |
|20                                    |Hawaiian Airlines                     |American                              |380000000                             |
```

Or we can also create a more specific filter by using the AND operator:

`SELECT * FROM operators WHERE nationality = 'American' AND capitalization > 500000000;`

And the result would be:
```
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|ID                                    |COMPANY_NAME                          |NATIONALITY                           |CAPITALIZATION                        |
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|12                                    |Delta Air Lines                       |American                              |550000000                             |
|13                                    |United Airlines                       |American                              |520000000                             |
```

### SELECT (Push query)
We can also run a push query on ksqlDB by issuing `SELECT * FROM operators EMIT CHANGES;` 
This query will intercept the changes in the operators stream. So each time a new row is added to the stream we will see a message on the console where the query is running. 

So if we insert a new row in the Postgres database like:

`INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Wizz Air', 'Hungarian', 135000000);`

The push query in ksqlDB should output:

```
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|ID                                    |COMPANY_NAME                          |NATIONALITY                           |CAPITALIZATION                        |
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|84                                    |Wizz Air                              |Hungarian                             |135000000                             |
```

This query stays active forever until we terminate it with `ctrl+c`


### CREATE STREAM AS SELECT
By running the following creary on ksqlDB we will create a new stream called `uae_operators` backed by the topic `uae_operators`. 
```
CREATE STREAM uae_operators
    WITH (
        KAFKA_TOPIC='uae_operators',
        KEY_FORMAT='AVRO',
        VALUE_FORMAT='AVRO',
        PARTITIONS=1
    ) AS 
        SELECT *
        FROM operators 
        WHERE nationality = 'UAE'
        EMIT CHANGES;
```
At the start the new stream `uae_operators` should be empty but if we add a new row into Postgres like this:

`INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Flydubai', 'UAE', 180000000);`

And then we run the following pull query in ksqlDB ```SELECT * FROM uae_operators;``` we should get the following results:

```
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|ID                                    |COMPANY_NAME                          |NATIONALITY                           |CAPITALIZATION                        |
+--------------------------------------+--------------------------------------+--------------------------------------+--------------------------------------+
|85                                    |Flydubai                              |UAE                                   |180000000                             |
```

### CREATE TABLE AS SELECT
Let's create a new table `count_operators_per_nationality` along with its underlying topic (`count_operators_per_nationality`), by executing the following query on ksqlDB :

```
CREATE TABLE count_operators_per_nationality
    WITH (
        KAFKA_TOPIC='count_operators_per_nationality',
        KEY_FORMAT='AVRO',
        VALUE_FORMAT='AVRO',
        PARTITIONS=1
    ) AS 
        SELECT nationality, count(*) AS count
        FROM operators 
        GROUP BY nationality
        EMIT CHANGES;
```

Now to test this new table let's execute the following statements inside the Postgres database:

```
INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Eurostar', 'British', 180000000);
INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Italo', 'Italian', 220000000);
INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Deutsch Bahn', 'German', 350000000);
INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Flixtrain', 'German', 260000000);
INSERT INTO operators (company_name, nationality, capitalization) VALUES ('Trenord', 'Italian', 160000000); 
```

Now let's execute this query on ksqlDB to check if the data have been uploaded correctly into the table:

`SELECT * FROM count_operators_per_nationality;`

and the result should be:

```
+-------------------------------------------------------------------------------+-------------------------------------------------------------------------------+
|NATIONALITY                                                                    |COUNT                                                                          |
+-------------------------------------------------------------------------------+-------------------------------------------------------------------------------+
|German                                                                         |2                                                                              |
|British                                                                        |1                                                                              |
|Italian                                                                        |2                                                                              |
```

The backing topic should be a compacted topic. To check the data into the topic we can create a consumer with the following line inside the schema registry container:

`kafka-avro-console-consumer --bootstrap-server broker:29092 --from-beginning --topic count_operators_per_nationality  --property print.key=true --property schema.registry.url=http://schema-registry:8081`

### Useful KSQL commands
- ***SHOW TOPICS***: lists the available topics in the Kafka cluster that ksqlDB is configured to connect to
- ***DROP STREAM***: drops a streams
- ***DROP TABLE***: drops a streams
- ***SHOW STREAMS***: lists the running streams on the cluster
- ***SHOW TABLES***: lists the running tables on the cluster
- ***SHOW QUERIES***: lists queries running in the cluster
- ***EXPLAIN***: Show the execution plan for a SQL expression or, given the ID of a running query, show the execution plan plus additional runtime information and metrics
- ***DESCRIBE***: List the columns in a stream or table along with their data type and other attributes, or list the relevant details for all streams and tables

### KSQLDB Documentantion Link
[KsqlDB documentation](https://docs.ksqldb.io/en/latest/developer-guide/ksqldb-reference/)

## Notes
To check in detail the Kafka cluster you can either use [Kadeck](https://www.kadeck.com/) or the Confluent Control Center at http://localhost:9021.
