package rosariob.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.eclipse.paho.client.mqttv3.*;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solution.model.PositionKey;
import solution.model.PositionValue;

public class VehiclePositionProducer {
    private static Logger logger = LoggerFactory.getLogger(VehiclePositionProducer.class);

    public static void main(String[] args) throws MqttException {
        logger.info("*** Starting VP Producer ***");

        Properties settings = new Properties();
        settings.put(ProducerConfig.CLIENT_ID_CONFIG, "vp-producer-avro");
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // localhost:9092 local, broker:29092 docker
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        settings.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"); // localhost:8081 local, schema-registry:8081 docker

        final KafkaProducer<PositionKey, PositionValue> producer = new KafkaProducer<>(settings);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("### Stopping VP Producer ###");
            producer.close();
        }));
        
        Subscriber subscriber = new Subscriber(producer);
        subscriber.start();
    }
}