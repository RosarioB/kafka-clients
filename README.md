# kafka-clients
I have grouped in this repository some examples of producers, consumers trying to explore the different possibilities that Kafka has to offer. 

The file docker-compose.yml, which contains the kafka infrastructure and all its services, has been used with all clients and it has been taken [here](https://github.com/confluentinc/cp-all-in-one/blob/7.4.0-post/cp-all-in-one/docker-compose.yml) .

There are different branches with different purposes:

- ***producer-consumer***: contains a basic producers which reads data from which receives IOT data from [this api](https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions/) and produces the events to the topic `vehicle-positions` and  a basic consumer which reads the data from the same topic and prints them to the stdout.

- ***producer-consumer-avro***: it is the same couple of producer and consumer of the precedent branch but this tijme it has been introduced Avro to handle the schema.

- ***kafka-streams***: contains a Kafka Streams applications and the same producer of the branch producer-consumer. The Kafka Streams applications reads the data from a topic Kafka, performs some transformations and then writes the output to another Kafka topic. The producer it is used just to import some data inside the kafka cluster and allows us to test the Kafka Streams applications.

- ***scaling-kafka-streams***: it is useful to demonstrate how a Kafka Streaqms application can be scaled.

-***kafka-connect***: it uses a Kafka Connect application to connect to a Postgres database via JDBC Connector (sink) and import the data inside a topic Kafka. 

Each branch has its own README files which explains in details what the applications have been developed for and how to use them.
