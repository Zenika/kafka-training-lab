package com.zenika.lab;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloKafkaConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(HelloKafkaConsumer.class);

  public static void main(String[] args) {
    Properties properties = loadApplicationProperties();

    // Write a Kafka Consumer that consumes and logs all records
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
