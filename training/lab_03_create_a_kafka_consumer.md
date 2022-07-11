# Lab 03 - Create a Kafka Consumer

## Kafka Consumer API

Let's write a Kafka consumer using Kakfa's java API.

See the project `consumer`. This project contains the code skeleton, it only needs to be completed.

The project contains:

* A class, [com.zenika.lab.HelloKafkaConsumer](../consumer/src/main/java/com/zenika/lab/HelloKafkaConsumer.java), that you
  need to complete.
* A [build.gradle](../consumer/build.gradle) file that contains the dependencies.
* A [property file](../consumer/src/main/resources/application.properties).

Note that the project has already been set up to use `slf4j` - that is a prerequisite for the java Kafka library.

### Adding the Kafka clients library

For this lab, Kafka client libraries have already been added to the classpath.

### Adding the Kafka properties

Useful note: the message we will consume have a `String` key and a `String` value. So we will use the
[StringDeserializer](https://kafka.apache.org/32/javadoc/org/apache/kafka/common/serialization/StringDeserializer.html).

Using Kafka means setting a lot of properties.
See the [documentation](https://kafka.apache.org/documentation.html) to have a description of each property.

Add the relevant properties to the `application.properties` file:

* `group.id` the group your consumer belongs to
* `bootstrap.servers` to set the address of your Kafka cluster
* `key.deserializer` full classpath to specify which deserializer you use for the key...
* `value.deserializer` ... and the value
* `auto.offset.reset` to `earliest`
* `fetch.max.wait.ms` to 500
* `fetch.min.bytes` to `16384`
* `isolation.level` to `read_committed`

See [consumer-completed](../consumer-completed).

The `auto.offset.reset` is set to `earliest`: it's like setting `--from-beginning` in the Kafka CLI, if the consumer
group is unknown or if the offset is missing the group will be reset to the beginning of the messages.

### Create the Kafka consumer

To consume data from Kafka you need to declare a `KafkaConsumer`. This consumer is not thread-safe, but it is auto-closable.
It takes some properties as a parameter.
It is typed for the key, and the value. For this lab, you will use String for both.

```java
try(KafkaConsumer<String, String> consumer=new KafkaConsumer<>(properties)){
      }
```

### Subscribe to the topic

You need to subscribe to the appropriate topic, in this case, `hello-world`.
Subscribing can be done using the `subscribe()` method of the `KafkaConsumer`.
Since a consumer can subscribe to multiple topics at once, you can either use a `List` to enumerate your topic, or use a
`java.util.regex.Pattern`.
This can be useful, especially if you have to consume an unknown number of topics. Instead of
writing `consumer.subscribe(List.of("order_fr", "order_es", "order_de", ...))`
you can write `consumer.subscribe(Pattern.compile("order*"))`.

### Consume the messages

The consumer is not thread-safe. If you want to use it, you will need to use it with a `while(true)` loop. Thankfully,
most frameworks do this for you (such as `Spring` and `Quarkus`). For this lab, you'll do it by hand. So start by writing
this infinite loop.

Then, you need to actually request the next messages using the `poll()` method from the `KafkaConsumer` class.
The `poll()` method doesn't actually send a request to the cluster immediately. Instead, it sends several parallel `fetch`
requests to several brokers of the cluster. Each fetch request will come back with a certain number of messages across
several partitions for the subscribed topics.

The method `poll()` returns `KafkaRecords`, a list of records. Order of these records is preserved in a single partition.

One last thing. The `poll()` methods accepts an argument, a Duration, which is the time the method will block is no
records are available. In order to avoid spamming the cluster in case no messages are available, set it to a low value
like 200 ms.

```java
while(true){
      ConsumerRecords<String, String> records=consumer.poll(Duration.ofMillis(200));
      }
```

Once the messages are polled, you can process them using a regular for loop. Start by logging the content of each message:

```java
for(ConsumerRecord<String, String> record:records){
      LOG.info("Received message {} - {}",record.key(),record.value());
      }
```

Run the consumer using `./gradlew consumer:run`.

The messages should be consumed:

```text
15:55:30.699 [main] INFO  com.zenika.lab.HelloKafkaConsumer - Received message hello - 757
15:55:30.699 [main] INFO  com.zenika.lab.HelloKafkaConsumer - Received message hello - 3260
15:55:30.699 [main] INFO  com.zenika.lab.HelloKafkaConsumer - Received message hello - 5539
```

### Run several consumers in parallel

The topic `hello-world` is created with 3 partitions. That means that 3 consumers of the same group can consume its data
in parallel. Open new terminal tabs and run two additional consumers. All 3 consumers will share the data in the topic.

Then run again the producer and sends 100 messages. The messages are sent to a single partition (they all share the same
key). See which consumer picks up these messages. Then, set the key as random (like the value), and rerun the producer.
Messages should be dispatched between the 3 partitions - and thus, the three consumers.

Stop the 3 consumers.

### See the consumers in AKHQ

In [AKHQ](http://localhost:8085), you can see the consumer groups. You'll notice a single consumer group exists,
named `hello-kafka-consumer` - like the `group.id` property.
You can see the `lag` of the consumers, which indicates how many messages have not yet been consumed by this consumer.
It will be often necessary to adjust the lag. Imagine an application crashing and needed to replay yesterday's messages in
Kafka. Luckily, this is possible in Kafka.

### Delete the consumer group

You can delete the consumer group in AKHQ. In this case, when the consumer will reconnect, it will start to either the
beginning or the end of the logs, according to the configuration of the `auto.offset.reset` config.
Don't do this right now - only know it's possible.

### Modify the consumer group

You can also change the consumer group (if the consumer is stopped) and set the lag manually. This is possible using AKHQ,
but you will practice this using the `kafka-consumer-groups` CLI.
Connect to the Kafka using docker: `docker exec -it kafka sh`.
Then, run the following command (in dry-run):

```shell
kafka-consumer-groups \
  --bootstrap-server kafka:9092 \
  --topic hello-world \
  --group hello-kafka-consumer \
  --reset-offsets \
  --to-earliest \
  --dry-run
```

The command will output how the offsets will be changed:

```text
GROUP                          TOPIC                          PARTITION  NEW-OFFSET     
hello-kafka-consumer           hello-world                    0          500            
hello-kafka-consumer           hello-world                    1          2              
hello-kafka-consumer           hello-world                    2          0      
```

Let's run the command using the `execute` option:

```shell
kafka-consumer-groups \
  --bootstrap-server kafka:9092 \
  --topic hello-world \
  --group hello-kafka-consumer \
  --reset-offsets \
  --to-earliest \
  --execute
```

Check on AKHQ that the lag is non zero for this consumer group.
You can restart a single consumer to check that all messages of the topics are consumed again.
Stop the consumer once you consumed everything.