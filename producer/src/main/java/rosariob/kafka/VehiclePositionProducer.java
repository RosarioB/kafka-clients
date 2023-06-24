package rosariob.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehiclePositionProducer {
    private static Logger logger = LoggerFactory.getLogger(VehiclePositionProducer.class);

    public static void main(String[] args) throws MqttException {
        logger.info("*** Starting VP Producer ***");

        Properties settings = new Properties();
        settings.put(ProducerConfig.CLIENT_ID_CONFIG, "vp-producer");
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // localhost:9092 local, broker:29092 docker
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final KafkaProducer<String, String> producer = new KafkaProducer<>(settings);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("### Stopping VP Producer ###");
            producer.close();
        }));

        Subscriber subscriber = new Subscriber(producer);
        subscriber.start();
    }
}