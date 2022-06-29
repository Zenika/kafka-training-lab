# Lab 04 - Write the Avro schemas

## Goal

The goal of this lab is to write two Avro schemas. These schemas will be used later in the course. These schemas will
generate corresponding java classes which will be used later.

## The screening schemas

This lab will focus on a small business case resolving around movie screening. A cinema can have multiple screening a
week of different movies. The ultimate goal of the labs is to share this screening data between each cinema's scheduling
system, which is an ERP, and the website which must display each screening. The operational team that work in each cinema
has control over the screening (what movie they want to screen and at what time) whereas the website team needs this data
to show the schedule to the ticket buyers.

## First look at the schemas project

Let's look at the [schemas](../schemas) project. This project holds the definition of every schema we will use in our
application. Schemas are meant to be exchanged between projects, so it is a good thing they are isolated in their own
project. In our labs, the schemas will be shared between the producer (each cinema) and the consumer (the website).

Since both the producer and the consumer uses java, not only the definition of the schema (in Avro) can be shared, but
also the corresponding generated java classes.

The goal of the `schemas` project is to generate the java classes corresponding to the Avro schemas and to publish the
Avro schema in the Schema Registry.

### The build.gradle file

The [build.gradle](../schemas/build.gradle) file has been already configured for you.
Please note:

* A dependency to Avro, needed to generate the java class based on the avro file
* A gradle plugin, used to do the generation at build time
* A custom gradle task which publishes the schemas in the Schema Registry using its REST API

## Writing the schemas

Let's write the schemas. The schemas will be located in [src/main/avro](../schemas/src/main/avro).
We will write two schemas: one schema to describe a movie screening in itself, and one schema for the screening Id.
Let's start with the simplest schema: the screening Id. It's just a string id.

Write a file named `screeningId.avsc` in `src/main/avro`:

```json
{
  "namespace": "com.zenika.labs.movie",
  "name": "ScreeningId",
  "doc": "A screening Id",
  "type": "record",
  "fields": [
    {
      "name": "screeningId",
      "type": "string"
    }
  ]
}
```

`avsc` stands for `Avro Schema`. An avro schema starts with the namespace and the name, which will match the generated
java class. `doc` fields can be added to describe the schema and the fields. The type, `record`, will translate to a java
class. The `fields` define the list of fields of the class with their name and type.

Then, let's write a screening in itself. Write a file named `screening.avsc`:

```json
{
  "namespace": "com.zenika.labs.movie",
  "name": "Screening",
  "doc": "A movie screens at a specific date at a specific cinema",
  "type": "record",
  "fields": [
    {
      "name": "screeningId",
      "type": "string"
    },
    {
      "name": "movieId",
      "type": "string"
    },
    {
      "name": "screeningDate",
      "type": {
        "type": "int",
        "logicalType": "date"
      }
    },
    {
      "name": "cinemaId",
      "type": "string"
    }
  ]
}
```

## Generating the java classes

Now that the schemas are written, java class can be generated from the schemas. For this, you just need to run a gradle
build, and the plugin will do the heavy lifting:

```shell
./gradlew schemas:build
```

Check in the `build` folder. A folder named `generated-main-avro-java` has been created, with generated sources. These
sources include a `Screening` class and a `ScreeningId` class.
When you export your project, as a jar, teams that need it (such as the website team) just needs to add it as one of
their dependency to their build.

## Registering the schemas into the Schema Registry

The schemas are defined, but they are not in the Schema Registry yet. If you would just use these schemas as is, the
producer would push the schema in the registry. That is not a good practice, instead the recommended approcha is to push
the schema in the Schema Registry using the CI/CD.

If you check on [AKHQ](http://localhost:8085), you'll notice the schemas are missing.

A custom gradle task has been created to push the schema. Run it:

```shell
./gradlew schemas:registerSchemas
```

Both schemas will be registered in the Schema Registry (check it using AKHQ).

```text
Registering the schema [...]schemas-completed/src/main/avro/screeningId.avsc
{"id":1}
Registering the schema [...]schemas-completed/src/main/avro/screening.avsc
{"id":2}
```

### Playing with schema compatibility

The Schema Registry is `BACKWARD` compatible (see the setting `SCHEMA_REGISTRY_SCHEMA_COMPATIBILITY_LEVEL` in
the [docker-compose.yml file](../dev/docker-compose.yml)). This means that a consumer using a new
version of the schema
can read messages written with a previous version of the schema. Adding fields in a schema with no default is a
`BACKWARD` breaking change: a consumer using the new version of the schema can't read messages that miss this field.

Let's prove it by adding a string field, `temp`, to our `screening.avsc` schema:

```json
{
  "namespace": "com.zenika.labs.movie",
  "name": "Screening",
  "doc": "A movie screens at a specific date at a specific cinema",
  "type": "record",
  "fields": [
    [
      ...
    ],
    {
      "name": "temp",
      "type": "string"
    }
  ]
}
```

Try to register the schema using the gradle task:

```shell
./gradlew schemas:registerSchemas
```

You'll get a 409 (Conflict) exception:

```text
> java.io.IOException: Server returned HTTP response code: 409 for URL: http://localhost:8081/subjects/screening-value/versions
```

Now, if the field has a default value, then you can read all previous versions of the schema with the new schema version.
Let's do this:

```json
{
  "namespace": "com.zenika.labs.movie",
  "name": "Screening",
  "doc": "A movie screens at a specific date at a specific cinema",
  "type": "record",
  "fields": [
    [
      ...
    ],
    {
      "name": "temp",
      "type": "string",
      "default": "0"
    }
  ]
}
```

Try to register the schema again. This should work.
For now, we will not use the version with the `temp` field. Remove the field from the schema.
Delete the schema in the Schema Registry using these commands:

```shell
curl -XDELETE \
  -H "Accept: application/json" \
  http://schema-registry:8081/subjects/screening-value

curl -XDELETE \
  -H "Accept: application/json" \
  http://schema-registry:8081/subjects/screening-value?permanent=true
```

Note that you need to run 2 distinct curls to delete the schema: one to do a soft delete, and then one to do a hard delete.