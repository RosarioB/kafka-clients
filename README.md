# kafka-clients
I have grouped in this repository some examples of producers, consumers trying to explore the different possibilities that Kafka has to offer. 

I have followed the course ***On demand - Confluent developer skills for building apache kafka*** by Confluent and some of these examples are solutions to the course exercises.

There are different branches with different purposes:

- ***producer***: it contains a basic producer which receives IOT data from [this api](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) with a MqttClient.

  Before starting the client we need to run `docker-compose -f kafka-docker-compose.yml up`, inside the folder docker to start the kafka infrastructure with all its services.

  After that we need to create the topic `vehicle-postions`. To do that we can open the bash sehll inside the `broker` container with: `docker exec -it broker bash ` and then execute 

  `kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions` to create the topic.
