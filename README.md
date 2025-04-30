[<img src="./images/logo.png" width="400" height="200"/>](./images/logo.png)

# api-common
[![Maven Build & Sonar Analysis](https://github.com/eclipse-ecsp/api-common/actions/workflows/maven-build.yml/badge.svg)](https://github.com/eclipse-ecsp/api-common/actions/workflows/maven-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eclipse-ecsp_api-common&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eclipse-ecsp_api-common)
[![License Compliance](https://github.com/eclipse-ecsp/api-common/actions/workflows/licence-compliance.yaml/badge.svg)](https://github.com/eclipse-ecsp/api-common/actions/workflows/licence-compliance.yaml)
[![Latest Release](https://img.shields.io/github/v/release/eclipse-ecsp/api-common?sort=semver)](https://github.com/eclipse-ecsp/api-common/releases)

api-common is a Java library that is used in the API microservices. It provides some common and necessary functionality to microservices that expose REST APIs.

It includes the following:
* Application to start the Spring application with added configuration
* Kafka producer service to push message to the Kafka cluster
* Capture metrics for API processing during histogram, API request in progress gauge, API request counter, api-gc, api-memory and api-threads
* Health monitors to help initial and periodic health checks
* Exposes GET /metrics and GET /v1/jamon-metrics for performance monitoring
* RestControllerAdvice for handling common exceptions that convert exceptions to meaningful JSON responses
* ApiPagination utility for creating paginated ignite criteria query
* ApiUtils for preparing a common header for responses
* JsonUtils for providing JSON serialization and deserialization
* ObjectUtils for providing common object validations

# Table of Contents
* [Getting Started](#getting-started)
* [Usage](#usage)
* [How to contribute](#how-to-contribute)
* [Built with Dependencies](#built-with-dependencies)
* [Code of Conduct](#code-of-conduct)
* [Authors](#authors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)


## Getting Started

To build the project in the local working directory after the project has been cloned/forked, run the following from the command line interface.

```mvn clean install```

from the command line interface.

### Prerequisites

You need the following to install the software:
* [Java jdk 17+](https://jdk.java.net/archive/) 
* [Maven 3.6](https://maven.apache.org/)

#### dependencies on other modules

* parent pom: 
the version of other modules and 3rd-party library are in services-dependencies

```xml
 <parent>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>services-dependencies</artifactId>
    <version>1.0.X</version> <!-- release version -->
    <relativePath/> <!-- lookup parent from repository -->
  </parent>
```
* ecsp cache-enabler

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>cache-enabler</artifactId>
 </dependency>
```

* ecsp nosql-dao

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>nosql-dao</artifactId>
  </dependency>
```

* ecsp utils

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>utils</artifactId>
  </dependency>
```

### Installation

The following is a step-by-step series of examples that describe how to get a local development environment running.

Step 1: build

```shell
$ mvn -s settings.xml clean install -Dmaven.test.skip=<true/false>
```

Step 2 : Release

```shell
$ mvn -s settings.xml 

$ mvn -s settings.xml clean deploy -P release -Drevision=<RELEASE_VERSION>"
```

Step 3 : add api-common dependency to api microservices

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>api-common</artifactId>
    <version>1.0.XX</version> <!-- release version -->
  </dependency>
```

### Coding style check configuration

Describe the details for configuring a style check in the IDE.
* Eclipse: You can install the tool via the marketplace for Checkstyle Marketplace entry Or you can install into via Help  Install new Software menu entry by using the following update site: https://checkstyle.org/eclipse-cs-update-site
* IntelliJ: Checkstyle plugin in IntelliJ provides both real-time and on-demand scanning of Java files with Checkstyle from within IDEA.

#### Using Checkstyle in the Eclipse IDE
1. Right-click on your project and select Checkstyle. Check code with Checkstyle.<br/>
2. Then open the Checkstyle views to view the reported issues via Windows Show View Others  Checkstyle menu.<br/>
3. Open the Checkstyle violations view to view the details or the Checkstyle violations chart to view a graphical overview of the issues.<br/>
4. In the Checkstyle violations view, double-click a violation to display an individual issue. Double-click an individual issue to display it. Use the **Back** button to return to the previous page.<br/>

#### Add checkstyle.xml to project
1. After installing the Checkstype plugin and restarting the IDE, you can view Checkstyle in the bottom tab.
2. To add a custom checkstyle.xml, click the PLUS (+) in File → Settings → Tools → Checkstyle.
3. Select the **dir** icon to run for entire project or run with the **PLAY** button for a single file opened in the tab.<br>

#### Coding style check configuration
[checkstyle.xml](./checkstyle.xml) is the coding standard to follow while writing new/updating existing code.

Checkstyle plugin [maven-checkstyle-plugin:3.3.1](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is integrated in [pom.xml](./pom.xml) which runs in the `validate` phase and `check` goal of the maven lifecycle and fails the build if there are any checkstyle errors in the project.

### Running the tests

Execute the Unit Test cases

```shell
mvn test
```

## Usage

Library usage documentation.

Add api-common dependency to the API microservices.

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>api-common</artifactId>
    <version>1.0.XX</version> <!-- release version -->
  </dependency>
```

#### Configure the application base package
the main class Application initialize and scan component under package org.eclipse.ecsp

to create and configure application bean available in different package (e.g. com.example), below config can be used to include the application package
```properties
base.package=org.eclipse.ecsp
```

#### Kafka Service:
Kafka service to push message to the Kafka cluster. below are the required properties for kafka service.
```properties
kafka.sink.topic=KafkaTopicName # kafka topic name
kafka.producer.synchronous.push=<true/false> # if enabled, message will be published in sync manner. 
```

```java
     @Autowire
     private KafkaService kafkaService;

    //send messages to kafka source topic, default key = igniteEvent.vehicleId
    kafkaService.sendIgniteEvent(igniteEvent);

    //send messages to kafka topic
    kafkaService.sendIgniteEvent(key,igniteEvent,topicName);

```

#### RestTemplate Configuration
```properties
rest.client.read.timeout=3000
rest.client.connection.timeout=3000
rest.client.connection.request.timeout=3000
rest.client.max.conn.total=20
rest.client.max.conn.per.route=2
```      

#### Metrics

* api_requests_total - Counter for requests <br/>
* api_processing_duration_seconds - Histogram for API processing duration in seconds<br/>
* api_inprogress_requests - Gauge for number of requests being served at this instant<br/>
* api-gc - garbage collector metrics
* api-memory - memory usage gauge
* api-threads - thread state gauge

```properties
metrics.enabled=true
processing.duration.buckets=0.05,0.1,0.2,0.3,0.4,0.7,1,2.5,5,10
performance.monitoring.enabled=false // for legacy support
performance.pointcut.expression=execution(* org.eclipse.ecsp..*.*(..))
#update according as per the application:
#performance.pointcut.expression=execution(* com.example..*.*(..)) || execution(* org.eclipse.ecsp..*.*(..))
```
#### Health Check Configurations
```properties
health.service.failure.retry.thrshold=19
health.service.failure.retry.interval.millis=50
health.service.retry.interval.millis=100
health.service.executor.shutdown.millis=20000
health.service.executor.initial.delay=300000
health.redis.monitor.enabled=true
health.redis.needs.restart.on.failure=false
health.mongo.monitor.enabled=true
health.mongo.needs.restart.on.failure=true
```

#### Web Config

```properties
cors.origin.allow=*
service.name=serviceName
node.name=abc
```
## Built With Dependencies

* [Spring Boot](https://spring.io/projects/spring-boot/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management

## How to contribute

See [CONTRIBUTING.md](./CONTRIBUTING.md) for details about our contribution guidelines and the process for submitting pull requests to us.

## Code of Conduct

See [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details about our code of conduct and the process for submitting pull requests to us.


## Authors

* **Eugene Jiang** - *Initial work*
* **[Abhishek Kumar](https://github.com/abhishekkumar-harman)** - *Initial work*
* **Charles Zhu** - *Initial work*
* **Leon Chen** - *Initial work*
* **Shubhangi Shukla** - *Initial work*
* **Padmaja Ainapure** - *Initial work*

For a list contributors to this project, see the [list of contributors](../../graphs/contributors).

## Security Contact Information

See [SECURITY.md](./SECURITY.md) to raise any security-related issues.

## Support
Contact the project developers via the project's "dev" list - https://accounts.eclipse.org/mailing-list/ecsp-dev

## Troubleshooting

See [CONTRIBUTING.md](./CONTRIBUTING.md) for details about raising issues and submitting pull requests to us.

## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.

## Announcements

All updates to this library are documented in [releases](../../releases).
For the available versions, see the [tags on this repository](../../tags).