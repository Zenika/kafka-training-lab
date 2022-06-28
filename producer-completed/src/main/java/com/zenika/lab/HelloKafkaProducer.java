package com.zenika.lab;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloKafkaProducer {
  private static final Logger LOG = LoggerFactory.getLogger(HelloKafkaProducer.class);

  public static void main(String[] args) {
    Properties properties = loadApplicationProperties();

    try(KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties)) {
      for (int i = 0; i < 100; i++) {
        int randomNumber = produceRandomNumber();
        ProducerRecord<String, String> record = new ProducerRecord<>(
              "hello-world", "hello", String.valueOf(randomNumber)
        );
        kafkaProducer.send(record, (metadata, exception) -> {
          if (exception != null) {
            LOG.error("Error while sending record {} to Kafka: {}", record.key(), exception);
          } else {
            LOG.info("Record {} - {} sent to Kafka", record.key(), record.value());
          }
        });
      }
    }
  }

  private static Properties loadApplicationProperties() {
    try {
      Properties properties = new Properties();
      properties.load(HelloKafkaProducer.class.getClassLoader().getResourceAsStream("application.properties"));
      return properties;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Random rng = new Random();

  private static int produceRandomNumber() {
    return rng.nextInt(10000);
  }
}
