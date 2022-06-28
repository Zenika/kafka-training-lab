package com.zenika.lab;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloKafka {
  private static final Logger LOG = LoggerFactory.getLogger(HelloKafka.class);

  public static void main(String[] args) {
    Properties properties = loadApplicationProperties();

    // Write a Kafka Producer that sends 100 records
    // Each record should have the key "hello" and a generated value converted to string
    // See helper method produceRandomNumber()
  }

  private static Properties loadApplicationProperties() {
    try {
      Properties properties = new Properties();
      properties.load(HelloKafka.class.getClassLoader().getResourceAsStream("application.properties"));
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
