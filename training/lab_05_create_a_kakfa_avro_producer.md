# Lab 05 - Create a Kafka Avro producer

## Goal

The goal of this lab is to reuse the Kafka producer, but using the Avro schemas you defined in the
previous labs.

The subproject used in this lab is the `screening-producer` subproject.

## Set up the required dependencies

The Schema Registry is not part of the open source Kafka distribution, but is a community product developed by Confluent.
For this reason, it is necessary first to get the required serializers that will convert the java objects to Avro bytes.

First, add the confluent repository to the [build.gradle file](../screening-producer/build.gradle).

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}
```

Then, add two dependencies to the project.
First, a dependency from this project to the schemas project:

```groovy
 implementation(project(":schemas"))
```

Then, a dependency to the `kafka-avro-serializer`:

```groovy
 implementation 'io.confluent:kafka-avro-serializer:7.1.1'
```

## Change the properties

The properties need to change. We are not using the String serializers anymore: instead, the `screening`
records we will send have both an Avro key and an Avro value.
The serializer we will use is this class: `io.confluent.kafka.serializers.KafkaAvroSerializer`. Set the correct
serializer for both the key and the value.

The producer will need to contact the Schema Registry to get the correct schema ID corresponding to this schema. In order
to do this, it needs to know the address of the registry. Set the following property:

```text
schema.registry.url=http://schema-registry:8081
```

As a best practice, you don't want the producer to auto register the schema. This should be done by the `schemas`
project's CI/CD. For this reason, configure the following property:

```text
auto.register.schemas=false
```

## Change the code

Now, update the code accordingly. You do not need to configure the `KafkaProducer` to use both `String` types. These
types can be replaced by the types you imported by adding the `project` dependency. Use the classes you generated like
`ScreeningId` and `Screening` directly.

The `ProducerRecord` will also need to be updated. For each record, create a key and a value. What data you put inside
the fields is not important:

```java
 var id=ScreeningId.newBuilder().setScreeningId(String.valueOf(produceRandomNumber())).build();
      var screening=Screening.newBuilder()
      .setScreeningId(id.getScreeningId())
      .setScreeningDate(LocalDate.now())
      .setCinemaId(String.valueOf(produceRandomNumber()))
      .setMovieId(String.valueOf(produceRandomNumber()))
      .build();
```

## Run the producer

Run the producer using the `gradle` CLI. Records should be produced using the schema you defined earlier:

```shell
./gradlew screening-producer:run
```

You should see the records in the `screening-topic` in AKHQ. Note that in AKHQ, records are printed as JSON String, but
they are in the Avro format.