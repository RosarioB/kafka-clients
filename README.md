# kafka-clients
I have grouped in this repository some examples of producers, consumers trying to explore the different possibilities that Kafka has to offer. 

I have followed the course ***On demand - Confluent developer skills for building apache kafka*** by Confluent and some of these examples are solutions to the course exercises.

Before starting the client it must be run `docker-compose -f kafka-docker-compose.yml up`, inside the folder docker to start the kafka infrastructure with all its services.

There are different branches with different purposes:

- ***producer***: it contains a basic producer which receives IOT data from [this api](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) with a MqttClient.
