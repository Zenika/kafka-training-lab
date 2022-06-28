<h1 align="center">Welcome to Kafka-training-lab 👋</h1>
<p>
  <a href="LICENSE" target="_blank">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />
  </a>
</p>

> Lab for Kafka Speed Training

## Setup

Prerequisite: have docker and docker-compose installed.

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

```text
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

```text
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

```text
kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic hello-world
```
Now, type your messages, press enter: your message is sent to Kafka. Press `CTRL-D` to exit.
```text
>hello
>world
> #CTRL-D PRESSED
```

Your messages can be seen in [AKHQ](http://localhost:8085). Click on the 🔎 next to the topic.

Your messages can also be consumed using another CLI: `kafka-console-consumer`.

```text
kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic hello-world \
  --from-beginning
```
Press `CTRL-C` to stop consuming messages. You should see the messages you sent earlier.
```text
hello
world
^CProcessed a total of 2 messages
```

## Author

👤 **Zenika**

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

This project is [MIT](LICENSE) licensed.

***
_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_