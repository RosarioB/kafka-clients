# branch consumer-producer
It contanins a producer and a consumer in the respective folders.

Before starting the client we need to run `docker-compose -f kafka-docker-compose.yml up -d` inside the folder docker to start the kafka infrastructure with all its services.

After that we need to create the topic `vehicle-positions`. To do that we can open the bash shell inside the `broker` container with: `docker exec -it broker bash ` and then execute: 

`kafka-topics --create --bootstrap-server broker:9092 --partitions 6 --replication-factor 1 --topic vehicle-positions` to create the topic.

## Producer

It contains a basic producer which receives IOT data from [this api](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) with a MqttClient.

To try the producer we have two possibilities:

- The first is to run the application (via java or the IDE itself) and setting `settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")` in the class `VehiclePositionproducer`.

- The second is to create a docker image from this project and the run the container.

  To create the docker image we must first set `settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:29092")` in the class `VehiclePositionproducer`. 

  Then we need to go to the producer folder and run `docker image build -t rosariob/producer:v2 .`

  To run the container with the previous image we can execute `docker run  --rm -d --network confluent_kafka --name producer rosariob/producer:v2`

## Consumer
 It contains a basic consumer which subscribes to the topic `vehicle-positions` and prints the messages to the stdout.

 To try the consumer we have two possibilities:
 
  - The first is to run the application (via java or the IDE itself) and setting `settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")` in the class `VehiclePositionConsumer`.

  - The second is to create a docker image from this project and the run the container.

    To create the docker image we must first set `settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:29092")` in the class `VehiclePositionConsumer`. 

    Then we need to go to the consumer folder and run `docker image build -t rosariob/consumer:v2 .`

    To run the container with the previous image we can execute `docker run  --rm -d --network confluent_kafka --name consumer rosariob/consumer:v2`
