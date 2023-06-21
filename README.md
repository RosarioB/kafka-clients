# kafka-clients
I have grouped in this repository some examples of producers, consumers trying to explore the different possibilities that Kafka has to offer. 

I have followed the course ***On demand - Confluent developer skills for building apache kafka*** by Confluent and some of these examples are solutions to the course exercises.

Before starting the client it must be run `docker-compose -f kafka-docker-compose.yml up`, inside the folder docker to start the kafka infrastructure with all its servers.

I have developed different branch which has different purposes. In the following there is a description of each branch:

- ***producer***: this is a basic producer which reads data from [this api](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) with a MqttClient.
