# Setup

## Prerequisites

* have docker and docker-compose installed.
* have a JDK 17 installed.

## Configure your host

Add the following to your host:

```text
127.0.0.1 Kafka
127.0.0.1 schema-registry
```

## Run docker-compose

Run the stack by going to the `dev` folder and using the following command:

`docker-compose up -d`

Go to [AKHQ](http://localhost:8085/ui). You should see a single topic, `hello-world`.

## Setup IntelliJ (or the editor of your choice)

Open the project using IntelliJ. It should detect a gradle multi-project build. You need not to have gradle
installed,
it's included in this project.

Let's do a proper health check: `./gradlew build` (if you are on windows, the command should be something along `gradlew.bat build`).
You should see something like this:
```shell
BUILD SUCCESSFUL in 3s
```
