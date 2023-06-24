package rosariob.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solution.model.PositionKey;
import solution.model.PositionValue;

public class VehiclePositionConsumer {
    private static Logger logger = LoggerFactory.getLogger(VehiclePositionConsumer.class);

    public static void main(String[] args) {
        logger.info("*** Starting VP Consumer ***");
        
        Properties settings = new Properties();
        settings.put(ConsumerConfig.GROUP_ID_CONFIG, "vp-consumer-avro");
        settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // localhost:9092 local, broker:29092 docker
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        settings.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        settings.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        settings.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        settings.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"); // localhost:8081 local, schema-registry:8081 docker

        KafkaConsumer<PositionKey, PositionValue> consumer = new KafkaConsumer<>(settings);
        try {
            consumer.subscribe(Arrays.asList("vehicle-positions-avro"));

            while (true) {
                ConsumerRecords<PositionKey, PositionValue> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<PositionKey, PositionValue> record : records)
                    System.out.printf("offset = %d, key = %s, value = %s\n",
                        record.offset(), record.key().toString(), record.value().toString());
            }
        }
        finally{
            logger.info("*** Ending VP Consumer ***");
            consumer.close();
        }
    }
}