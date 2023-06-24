# kafka-streams
It contanins a folder streams which contains the Kafka Streams application and a folder produver (which is the same of the branch producer-consumer) used to import import some data inside the Kafka cluster in the topic `vehicle-positions`.

The Kafka Streams applications is very easy it just reads the data from the topic `vehicle-positions`, filter it and then writes the new record in the topic `vehicle-positions-oper-47`.

Before starting the client we need to run `docker-compose -f kafka-docker-compose.yml up -d` inside the folder docker to start the kafka infrastructure with all its services.

After that we need to create the topic `vehicle-positions`. To do that we can open the bash shell inside the `broker` container with: `docker exec -it broker bash ` and then execute: 

`kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions` to create the topic.

Then we need to create the topic `vehicle-positions-oper-47` and this time we need to execute:

`kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions-oper-47` to create the topic.
