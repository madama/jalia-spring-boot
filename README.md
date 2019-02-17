[![Build Status](https://travis-ci.org/madama/jalia-spring-boot.svg?branch=master)](https://travis-ci.org/madama/jalia-spring-boot)


Jalia Spring Boot starter
=========================

Automatically sets up Jalia in a Spring Boot application, using existing JPA infrastructure if present. It also
sets up Jalia as default encoder/decoder for Flux based systems, inlcuding WebTestClient.

How to use it
=============

Just include this as a dependency to the pom.xml of you Spring Boot application:

```
<dependency>
    <groupId>net.etalia</groupId>
    <artifactId>jalia-spring-boot</artifactId>
    <version>${jalia.version}</version>
</dependency>
```

Or gradle project:
```
complie "net.etalia:jalia-spring-boot:${jalia-version}"
```

What will it do
===============

- Setup Jalia as the default JSON serializer and deserializer for you controllers.
- Setup Jalia as the serialized and deserializer for Flux based elements, like Flux webservers and the Webtestclient.
- Install web filter to parse the "fields" and "group" parameters.
- Automatically discover JPA entities and use the configured JPA EntityManager.
- Support @IdPathRequestBody annotation.

How to configure it
===================

Spring Boot integration will look for specific properties from application.properties to configure Jalia accordingly:

- `jalia.fieldsParameter`: the name of the fields parameter, defaults to "fields"
- `jalia.groupParameter`: the name of the group parameter, defaults to "group"
- `jalia.groupsResource`: Spring Resource pattern where to search for group definition files, defaults to
"classpath:/jalia/group*.json"
- `jalia.includeNulls`: see DefaultOptions.INCLUDE_NULLS
- `jalia.includeEmpty`: see DefaultOptions.INCLUDE_EMPTY
- `jalia.prettyPrint`: see DefaultOptions.PRETTY_PRINT
- `jalia.unrollObjects`: see DefaultOptions.UNROLL_OBJECTS
- `jalia.withPathRequestBody`: whether to setup support for @IdPathRequestBody, defaults to true
- `jalia.installConverter`: whether to setup the HTTP converter to use Jalia as default serializer and deserializer
for controllers, defaults to true
- `jalia.installCodec`: whether to setup the HTTP codecs to use Jalia as default serializer and deserializer on Flux
components, defaults to true

Configuring groups
==================

If not changed in configuration using `jalia.groupsResource`, Jalia will look for group definitions on the classpath,
in a folder named "jalia", for files matching "group*.json" glob pattersn.


