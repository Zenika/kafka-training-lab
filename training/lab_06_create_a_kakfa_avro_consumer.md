# Lab 06 - Create a Kafka Avro consumer

## Goal

The goal of this lab is to reuse the Kafka consumer, but using the Avro schemas you defined in the
previous labs.

The subproject used in this lab is the `website-consumer` subproject.

## Set up the required dependencies

This step is the same as the `Producer` lab. So, in this lab, this was already done for you.

## Change the properties

Like the producer, the properties need to change.
The deserializer we will use is this class: `io.confluent.kafka.serializers.KafkaAvroDeserializer`. Set the correct
deserializer for both the key and the value.

The consumer will also need to contact the Schema Registry to get the correct schema ID corresponding to this schema. In
order to do this, it needs to know the address of the registry. Set the following property:

```text
schema.registry.url=http://schema-registry:8081
```

The consumer needs to know if the deserialization will result in a `GenericReader` type or a specific user type. In our
case, we won't use the `GenericReader` API in our code and use the generated classes instead, so we need to configure
additional following property:

```text
specific.avro.reader=true
```

## Change the code

Now, update the code accordingly. The `KafkaConsumer` should not use `String` types, but our `Screening` classes. These
`String`
types can be replaced by the types you imported by adding the `project` dependency. Use the classes you generated like
`ScreeningId` and `Screening` directly.

The `ConsumerRecords` will also need to be updated. Print the content of each record key and value, like you did before.

## Run the consumer

Run the consumer using the `gradle` CLI. Records should be consumed using the schema you defined earlier:

```shell
./gradlew website-consumer:run
```

You should see the records printed as JSON String, but
they are in the Avro format.