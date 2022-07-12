package com.zenika.lab;

import com.zenika.labs.movie.Screening;
import com.zenika.labs.movie.ScreeningId;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloKafkaStream {

  private static final Logger LOG = LoggerFactory.getLogger(HelloKafkaStream.class);

  public static Topology buildTopology() {
    final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://schema-registry:8081");
    final Serde<ScreeningId> keySpecificAvroSerde = new SpecificAvroSerde<>();
    keySpecificAvroSerde.configure(serdeConfig, true);
    final Serde<Screening> valueSpecificAvroSerde = new SpecificAvroSerde<>();
    valueSpecificAvroSerde.configure(serdeConfig, false);

    StreamsBuilder builder = new StreamsBuilder();
    builder
          .stream("screening", Consumed.with(keySpecificAvroSerde, valueSpecificAvroSerde))
          //.peek((k,v) -> logger.info("Observed event: {}", v))
          //.mapValues(s -> s.toUpperCase())
          //.peek((k,v) -> logger.info("Transformed event: {}", v))
          .to("screening-2", Produced.with(keySpecificAvroSerde, valueSpecificAvroSerde));
    return builder.build();
  }

  public static void main(String[] args) {
    Properties props = loadApplicationProperties();

    var topology = buildTopology();

    LOG.info(topology.describe().toString());

    KafkaStreams streams = new KafkaStreams(
          topology,
          props);

    final CountDownLatch latch = new CountDownLatch(1);
    streams.setStateListener((newState, oldState) -> {
      if (oldState == KafkaStreams.State.RUNNING && newState != KafkaStreams.State.RUNNING) {
        latch.countDown();
      }
    });

    streams.start();

    try {
      latch.await();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  private static Properties loadApplicationProperties() {
    try {
      Properties properties = new Properties();

      properties.load(HelloKafkaStream.class.getClassLoader().getResourceAsStream("application.properties"));
      return properties;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
