# kafka-clients
I have grouped in this repository some examples of producers, consumers trying to explore the different possibilities that Kafka has to offer. 

I have followed the course ***On demand - Confluent developer skills for building apache kafka*** by Confluent and some of these examples are solutions to the course exercises.

The file docker-compose.yml, which contains the kafka infrastructure and all its services, has been used with all clients and it has been taken [here](https://github.com/confluentinc/cp-all-in-one/blob/7.4.0-post/cp-all-in-one/docker-compose.yml) .

There are different branches with different purposes:

- ***producer***: it contains a basic producer which receives IOT data from [this api](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) with a MqttClient.

  Before starting the client we need to run `docker-compose -f kafka-docker-compose.yml up -d` inside the folder docker to start the kafka infrastructure with all its services.

  After that we need to create the topic `vehicle-postions`. To do that we can open the bash shell inside the `broker` container with: `docker exec -it broker bash ` and then execute: 

  `kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions` to create the topic.

  To try the producer we have two possibilities:
  - one is to run the application (via java or the IDE itself) and setting `settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")` in the class `VehiclePositionproducer`.

  - The second is to create a docker image from this project and the run the container.

    To create the docker image we must first set `settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:29092")` in the class `VehiclePositionproducer`. 

    Then we need to go to the producer folder and run `docker image build -t rosariob/producer:v2 .`

    To run the container with the previous created image we can execute `docker run  --rm -d --network confluent_kafka --name producer rosariob/producer:v2`.
