# kafka-connect
This project aims at using Kafka Connect with the JDBC connector to import inside the Kafka topic `pg-operators` the data
from the operator table inside the postgres database.

This project contains three folders:
- docker: which contains the docker compose file.
- jars: which contains the jars of the [JDBC Connector](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc/)
- sql: which contains the script sql used to populate the postgres database.

To run the project we need to
1. run the docker compose file
2. populate the postgres database
3. configure Kafka connect to get the data from the postgres database

## Docker
To start the file kafka-docker-compose.yml we need to get inside the docker directory and run: `docker-compose -f kafka-docker-compose.yml up -d`

## Postgres
To enter in the postgres shell we can execute `docker exec -it postgres psql -U postgres`

Now we can create the `operators` table running:

```
CREATE TABLE operators(
    id int not null primary key,
    name varchar(50) not null
);
```

We need to set the password for the user postgres with this command. `ALTER USER postgres WITH PASSWORD 'password'`

In the folder sql there is a file script.sql which is needed to populate the operators table. 

To run this file we can execute: `\i sql/script.sql`

Notes:
- To view the list of the table we can execute `\dt`
- To view the list of databases we can execute `\l`

## Configure the source connector

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

To add the JDBC source connector which allows Kafka Connect to import the data from our Postgres database into the pg-operators topic
we need to make a POST request to the Kafka Connect REST API.

First we need to get inside the connect container with: `docker exec -it connect bash`

```
curl -s -X POST \
        -H "Content-Type: application/json" \
        --data '{
            "name": "Operators-Connector",
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

And it should print:
```
{
  "name": "Operators-Connector",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "connection.url": "jdbc:postgresql://postgres:5432/postgres",
    "connection.user": "postgres",
    "connection.password": "password",
    "table.whitelist": "operators",
    "mode": "incrementing",
    "incrementing.column.name": "id",
    "table.types": "TABLE",
    "topic.prefix": "pg-",
    "numeric.mapping": "best_fit",
    "transforms": "createKey,extractInt",
    "transforms.createKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields": "id",
    "transforms.extractInt.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractInt.field": "id",
    "name": "Operators-Connector"
  },
  "tasks": [],
  "type": "source"
}
```

Note: to delete the connector `curl -X DELETE http://connect:8083/connectors/Operators-Connector`

To verify that Kafka connect is importing record into the topic pg-operators we need to get inside the container schema-registry:
`docker exec -it schema-registry bash` and the run:

```
kafka-avro-console-consumer \
      --bootstrap-server broker:29092 \
      --property schema.registry.url=http://schema-registry:8081 \
      --topic pg-operators \
      --from-beginning \
      --property print.key=true
```

And this should be the output:

```
6       {"id":6,"name":"Oy Pohjolan Liikenne Ab"}
12      {"id":12,"name":"Helsingin Bussiliikenne Oy"}
17      {"id":17,"name":"Tammelundin Liikenne Oy"}
18      {"id":18,"name":"Pohjolan Kaupunkiliikenne Oy"}
19      {"id":19,"name":"Etelä-Suomen Linjaliikenne Oy"}
20      {"id":20,"name":"Bus Travel Åbergin Linja Oy"}
21      {"id":21,"name":"Bus Travel Oy Reissu Ruoti"}
22      {"id":22,"name":"Nobina Finland Oy"}
36      {"id":36,"name":"Nurmijärven Linja Oy"}
40      {"id":40,"name":"HKL-Raitioliikenne"}
45      {"id":45,"name":"Transdev Vantaa Oy"}
47      {"id":47,"name":"Taksikuljetus Oy"}
51      {"id":51,"name":"Korsisaari Oy"}
54      {"id":54,"name":"V-S Bussipalvelut Oy"}
55      {"id":55,"name":"Transdev Helsinki Oy"}
58      {"id":58,"name":"Koillisen Liikennepalvelut Oy"}
59      {"id":59,"name":"Tilausliikenne Nikkanen Oy"}
90      {"id":90,"name":"VR Oy"}
```