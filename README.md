<h1 align="center">Welcome to Kafka-training-lab 👋</h1>
<p>
  <a href="LICENSE" target="_blank">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />
  </a>
</p>

> Lab for Kafka Speed Training

## Setup

Prerequisites:
* have docker and docker-compose installed.
* have a JDK 17 installed.

Add the following to your host:

```text
127.0.0.1 Kafka
127.0.0.1 schema-registry
```

Run the stack by going to the `dev` folder and using the following command: 

`docker-compose up -d`

Go to [AKHQ](http://localhost:8085/ui). You should see a single topic, `hello-world`.

## What's inside the Kafka Stack

The [docker-compose file](./dev/docker-compose.yml) contains an entire Kafka environment.

A Kafka environment consists of:
- One or more brokers regrouped into a cluster. In this case, there is only a single broker, which exposes port 9092.
- A Zookeeper, it's role it's to keep Kafka's metadata. It's not used by Kafka Clients. It's port is 2181.
- [AKHQ](https://akhq.io/). This app allows you to see what's inside your cluster (port: 8085).
- The Schema Registry, which contains your environment schemas.
- A pod that auto creates topics. 
- A Postgres' database. That's only used for this lab.

## Let's create some topics

Let's create some topics using Kafka CLI. Connect to the Kafka broker:

`docker exec -it kafka sh`

When you install Kafka, all command line tools are installed by default in the `bin` folder. The `kafka-topic` tools 
allow you to create, update, delete, or describe a topic.

```shell
kafka-topics \
  --bootstrap-server kafka:9092 \
  --create \
  --topic my-first-topic \
  --partitions 3 \
  --replication-factor 1
```

If the creation is successful, a message should appear: `Created topic my-first-topic.`. Congratulations.

Note that the argument `bootstrap-server` was necessary. This can be any broker of the Kafka Cluster. This borker will be 
queried to discover other brokers in the cluster.
In production, it is recommended to use at east 3 brokers - that way, if one of the brokers is down, the others can still 
be used for the discovery. Example: `kafka-1:9092,kafka-2:9092,kakfa-3:9092`.

You can describe the topic you just created:

```shell
kafka-topics \
  --bootstrap-server kafka:9092 \
  --describe \
  --topic my-first-topic
```

Check the API of `kafka-topics` to see how you can delete the topic you created.

You can also use AKHQ to manage your topics. In production, using a tool like Terraform is recommended to let your CI/CD 
manage topics.

Topics can be auto-created by default by producer and consumers. This is a bad practice. This behaviour has been disabled 
in this environment, check the parameter `KAFKA_AUTO_CREATE_TOPICS_ENABLE` in the [docker-compose file](./dev/docker-compose.yml).

## Let's produce and consume

Other Kafka CLI are interesting.

`kafka-console-producer` is the tool used to push messages into Kafka.

```shell
kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic hello-world
```
Now, type your messages, press enter: your message is sent to Kafka. Press `CTRL-D` to exit.
```shell
>hello
>world
> #CTRL-D PRESSED
```

Your messages can be seen in [AKHQ](http://localhost:8085). Click on the 🔎 next to the topic.

Your messages can also be consumed using another CLI: `kafka-console-consumer`.

```shell
kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic hello-world \
  --from-beginning
```
Press `CTRL-C` to stop consuming messages. You should see the messages you sent earlier.
```shell
hello
world
^CProcessed a total of 2 messages
```
## Let's code

### Setup IntelliJ   (or the editor of your choice)

Open the project using IntelliJ. It should detect a gradle multi-project build. You need not to have gradle installed, 
it's included in this project.

Let's do a proper health check: `./gradlew build` (if you are on windows, the command should be something along `gradlew.bat build`).
You should see something like this:
```shell
BUILD SUCCESSFUL in 3s
```

### Kafka Producer API

Let's write a Kafka producer using Kakfa's java API.

See the project `producer`. This project contains the code skeleton, it only needs to be completed.

The project contains:
* A class, [com.zenika.lab.HelloKafka](./producer/src/main/java/com/zenika/lab/HelloKafka.java), that you need to complete.
* A [build.gradle](./producer/build.gradle) file that contains the dependencies.
* A [property file](./producer/src/main/resources/application.properties).

Note that the project has already been set up to use `slf4j` - that is a prerequisite for the java Kafka library.

#### Adding the Kafka clients library

First, let's add the `kafka-clients` library to the classpath. Add this dependency to your `build.gradle` file:

```groovy
implementation 'org.apache.kafka:kafka-clients:3.2.0'
```
Note that the version corresponds to the Kafka version.

If you are intelliJ, don't forget to reload your gradle environment.

#### Adding the Kafka properties

Useful note: the message we will produce will have a `String` key and a `String` value. So we will use the 
[StringSerializer](https://kafka.apache.org/32/javadoc/org/apache/kafka/common/serialization/StringSerializer.html).

Using Kafka means setting a lot of properties.
See the [documentation](https://kafka.apache.org/documentation.html) to have a description of each property.

Add the relevant properties to the `application.properties` file:
* `bootstrap.servers` to set the address of your Kafka cluster
* `key.serializer` full classpath to specify which serializer you use for the key...
* `value.serializer` ... and the value
* `acks` to all
* `enable.idempotence` to all
* `retries` to it's max value (2147483647)
* `batch.size` and `linger.ms` to set up batching
* `compression.type` to add compression (`snappy` is fine here)

See [producer-completed](./producer-completed) if you are struggling to find the proper values.

#### Create the Kafka producer

First, to use Kafka, you need to declare a `KafkaProducer`. This producer is thread-safe, and auto-closable. 
It takes some properties as a parameter. 
It is typed for the key, and the value. For this lab, you will use String for both.
```java
try(KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties)) {
  
}
```
#### Send the messages

The goal is to send 100 messages. Each message will have the same key, `hello-world`, and a random value generated by a 
call to the `produceRandomNumber()` helper method.

You'll send 100 messages, so first let's write a `for` loop. Then to send your messages, you'll need to do to things:
* Create a `ProducerRecord<String, String>`
* Call the `send()` method of the `KafkaProducer`.

The `ProducerRecord` will contain information about the message you are trying to send: the topic, the key, and the value
```java
ProducerRecord<String, String> record = new ProducerRecord<>(
  "topic", "key", "value"
);
```
Replace with the appropriate key and value.

Then, to send the message:
```java
kafkaProducer.send(record);
```
Note that when the `send` method is called, the message is not yet sent to Kafka. In practice, the message is added to an 
in-memory buffer. The message will be sent alongside other messages to the broker once the batch of messages it belongs 
to is ready (remember `batch.size` and `linger.ms` ?).

Calling `send()` is not enough to know if the message has been properly sent. Luckily, the Kafka library provides two 
ways of knowing if the message has been sent to Kafka. If you provide no additional argument than the record, then the 
send method returns a `Future<RecordMetadata>`. Since it's a java future, you can force waiting for the cluster to repond 
by using it's `get` method.

However, a better way exist. You can supply a lambda as the second argument of the `send()` method. This lambda must be a 
`Callback` and implement a single method, `void onCompletion(RecordMetadata metadata, Exception exception)`.
 If the exception is not `null`, this means there was an error sending the record to Kafka that your need to process in 
your applicative code. If the exception is `null`, you are certain the record was sent successfully and you can use the 
`RecordMetadata`.

For example, you could use it like this:
```java
kafkaProducer.send(record, (metadata, exception) -> {
  if (exception != null) {
    LOG.error("Error while sending record {} to Kafka: {}", record.key(), exception);
  } else {
    LOG.info("Record {} - {} sent to Kafka", record.key(), record.value());
  }
});
```

## Author

👤 **Zenika**

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

This project is [MIT](LICENSE) licensed.

***
_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_