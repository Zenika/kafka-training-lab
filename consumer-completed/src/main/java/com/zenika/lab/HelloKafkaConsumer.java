package com.zenika.lab;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloKafkaConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(HelloKafkaConsumer.class);

  public static void main(String[] args) {
    Properties properties = loadApplicationProperties();

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
      consumer.subscribe(List.of("hello-world"));

      while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
        for (ConsumerRecord<String, String> record : records) {
          LOG.info("Received message {} - {}", record.key(), record.value());
        }
      }
    }

  }

  private static Properties loadApplicationProperties() {
    try {
      Properties properties = new Properties();
      properties.load(HelloKafkaConsumer.class.getClassLoader().getResourceAsStream("application.properties"));
      return properties;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
