# kafka-streams
It contanins a producer and a consumer in the respective folders.

Before starting the client we need to run `docker-compose -f kafka-docker-compose.yml up -d` inside the folder docker to start the kafka infrastructure with all its services.

After that we need to create the topic `vehicle-positions`. To do that we can open the bash shell inside the `broker` container with: `docker exec -it broker bash ` and then execute: 

`kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions` to create the topic.

Then we need to create the topic `vehicle-positions-oper-47` and this time we need to execute:

`kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions-oper-47` to create the topic.