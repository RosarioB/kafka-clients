# scaling-kafka-streams
This branch demonstrates the scaling of a Kafka Streams application.

There are different folders which host different applications:
1. docker: contains the docker compose file
2. producer-python: contains the python producer
3. producer-java: contains the Java producer
4. streams: contains the Kafka Streams application

## Kafka containers
Before starting the client we need to run `docker-compose -f kafka-docker-compose.yml up -d` inside the folder docker to start the kafka infrastructure with all its services.

After that we need to create the topic `temperature-readings` and `max-temperatures`. To do that we can open the bash shell inside the `broker` container with: `docker exec -it broker bash` and then execute:

`kafka-topics --create --bootstrap-server broker:9092 --partitions 3 --replication-factor 1 --topic temperature-readings` to create the the first topic and

`kafka-topics --create --bootstrap-server broker:9092 --partitions 3 --replication-factor 1 --topic max-temperatures` to create the second topic.

## Python producer
To execute the python producer we must go to the folder producer-python and then execute:

`pip3 install confluent-kafka` to install the confluent dependencies and then

`python3 main.py` to execute the python producer.

To check if the producer is working fine we can open the broker's container bash shell:

`docker run -it broker bash` 

and then make use of the console producer:

`kafka-console-consumer --bootstrap-server broker:9092 --from-beginning --topic temperature-readings  --property print.key=true     --property key.separator=", "`

## Java producer
The java producer is inside the folder java-producer.

To execute the java producer we have two possibilities:

- The first is to run the application (via java or the IDE itself) and setting `props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); ` in the class `TempProducer`.


- The second is to create the package via maven from the folder producer-java with:

  `mvn clean package` and then run 

  `java -jar target/producer-java-1.0-SNAPSHOT.jar`

## Streams application
The Streams application is inside the folder streams.

This applications reads the data from the input topic `temperature-readings` and writes the output to the `max-temperatures` topic.

Similarly to the Java producer we have two ways to run this application:

To execute the java producer we have two possibilities:

- The first is to run the application (via java or the IDE itself) and setting `settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); ` in the class `StreamsApp`.


- The second is to create the package via maven from the folder producer-java with:

  `mvn clean package` and then run

  `java -jar target/streams-1.0-SNAPSHOT.jar <path_state_dir>` where <path_state_dir> is the path of the state store of the kafka 
   streams instance we are running. Please keep in mind that each kafka streams instance must point to a different directory otherwise
   there will be lock issues.
   For example in a mac computer it would be `java -jar streams-1.0-SNAPSHOT.jar /Users/rosarioborgesi/Desktop/prova_streams/state_dir`

## Test
The test aims at proving the Kafka Streams scalability. In order to prove the scalability we need to:
1. start the docker producer
2. start the Java producer
3. start one instance of the kafka streams application.

Then we can check at the Confluent Control Center at http://localhost:9021/ at the section Consumer, there is a consumer
group called streams-app-v.0.1.0. With one instance of the Kafka Streams application the message behind increase very quickly.

At this point we can start other two instances of the Kafka Streams application and see that the messages behind will grow more 
slowly as we add more instances.
However since we have set only 3 partitions on the input topic `temperature-readings` if we add more then 3 partitions 
we will not increase the scalability because the other instances would be idle.

## KSQL
Instead of running the Kafka Streams application we could have:

1. Connected to the KSQL server container `docker exec -it ksqldb-server bash`

2. Open the KSQL CLI at `ksql http://ksqldb-server:8088`

3. Set the starting point of the streams to earliest: `SET 'auto.offset.reset' = 'earliest';`

4. Create a stream for the source topic: 

   `CREATE STREAM temperatures(station STRING, temperature INTEGER) WITH(KAFKA_TOPIC='temperature-readings', VALUE_FORMAT='JSON');`

5. Create a table that shows the maximum, minimum, and average temperatures per station per minute: 

      `CREATE TABLE temp_agg_per_min AS 
         SELECT 
            station,
            max(temperature) AS max,
            min(temperature) AS min,
            sum(temperature) / count( * ) AS avg
         FROM temperatures
         WINDOW TUMBLING (SIZE 1 MINUTE)
         GROUP BY station;`
    
6. Inspect the aggregated temperature data as new records flow in from the producer.

    `SELECT station, max, min, avg FROM temp_agg_per_min;`