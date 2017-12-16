# KumuluzEE Reactive
[![Build Status](https://img.shields.io/travis/kumuluz/kumuluzee-reactive/master.svg?style=flat)](https://travis-ci.org/kumuluz/kumuluzee-reactive)

> KumuluzEE Reactive extension for developing reactive microservices.

KumuluzEE Reactive is an extension for using reactive patterns with the KumuluzEE microservices. It provides support for integrating KumuluzEE microservices with various reactive frameworks, such as Vert.x, ReactiveX, etc. KumuluzEE Reactive has been designed to support modularity with pluggable reactive frameworks. 

Currently, Vert.x is supported. In the future, other reactive frameworks will be supported too (contributions are welcome). 

# Using Vert.x with KumuluzEE microservices

KumuluzEE Reactive Vert.x provides integration with Vert.x distributed event bus and integration with Vert.x service discovery. It provides easy-to-use annotations for developing microservices that listen to or produce messages on the Vert.x distributed event bus and a service discovery bridge for importing and exporting services from Vert.x service discovery to KumuluzEE Service Discovery (for Consul and etcd).

## How it works

As shown in the image below, the microservice that is using KumuluzEE Reactive Vert.x extension communicates to other Vert.x instances via distributed event bus. Both have to be started in a mode called clustered, so that they are able to find each other. After that, they can start to listen to a given address or publish events on the event bus.

Vert.x passes all of its events to the event handlers when they are available. In most cases, Vert.x call handlers are using a thread called event loop. One Vert.x instance can maintain multiple event loops. This means that a single Vert.x process easily can scale across a server.

If you want to learn more, click [here](http://vertx.io/docs/vertx-core/java/).

![KumuluzEE Reactive Vert.x architecture](https://raw.githubusercontent.com/kumuluz/kumuluzee-reactive/master/resources/img/architecture.png)

## Usage

You can enable KumuluzEE Reactive with Vert.x by adding the following dependency:
```xml
<dependency>
	<groupId>com.kumuluz.ee.reactive</groupId>
	<artifactId>kumuluzee-reactive-vertx</artifactId>
	<version>${kumuluzee-reactive-vertx.version}</version>
</dependency>
```
## Configuring Vert.x 

Vert.x is configured with the common KumuluzEE configuration framework. Configuration properties can be defined with the environment variables or with the configuration files. Alternatively, they can also be stored in a configuration server, such as etcd or Consul (for which the KumuluzEE Config extension is required). For more details see the [KumuluzEE configuration wiki page](https://github.com/kumuluz/kumuluzee/wiki/Configuration) and [KumuluzEE Config](https://github.com/kumuluz/kumuluzee-config).

Listed below are all the properties available for Vert.x configuration:
```yaml
kumuluzee:
  env:
    name: dev
  reactive:
    vertx:
      blocked-thread-check-interval: 1000
      event-loop-pool-size: 16
      file-caching-enabled: true
      ha-enabled: false
      ha-group: __DEFAULT__
      internal-blocking-pool-size: 20
      max-event-loop-execute-time: 2000000000
      max-worker-execute-time: 60000000000
      quorum-size: 1
      worker-pool-size: 20
      clustered: true
      cluster-host: localhost
      cluster-port: 0
      cluster-ping-interval: 20000
      cluster-ping-reply-interval: 20000
      cluster-public-port: null
      cluster-public-port: -1
      discovery:
        env: 
          name: dev
        ttl: 30
        ping-interval: 10

```
Properties *blocked-thread-check-interval*, *cluster-ping-interval*, *cluster-ping-reply-interval*  and *scan-period* are set in **milliseconds**, while *max-event-loop-execute-time* and *max-worker-execute-time* are set in **nanoseconds**.

## Reactive Event Publisher annotation

For injecting the Vert.x event bus message producer, KumuluzEE Reactive provides a `@ReactiveEventPublisher` annotation which will inject a message producer. A use of `@Inject` annotation is also needed. The annotation accepts one parameter, which is by default set to `publisher`.

Example of using the following annotation:
```java
@Inject
@ReactiveEventPublisher(address = "event-name")
MessageProducer<Object> messageProducer;
```

## Reactive Event Listener annotation

For listening to Vert.x event bus, KumuluzEE Reactive provides the `@ReactiveEventListener` annotation. Use of `@Inject` annotation is also needed. The annotation accepts one parameter which is by default `listener`. The annotation itself can be used on top of any method as long as it has one parameter of type `Message<Object>`.  We can also reply to a message as shown in the example below.

Example of using the following annotation:
```java
@ReactiveEventListener(address = "event-name")
public void onMessage(Message<Object> event) {    
  if(event.body() != null) {
    event.reply("Message received.");
  }   
}
```

## Service Discovery Bridge

KumuluzEE Reactive extension provides a bridge between Vert.x Service Discovery and KumuluzEE Discovery (for etcd and Consul).
To enable the bridge import either one of the following dependencies
 - `kumuluzee-discovery-etcd`
 - `kumuluzee-discovery-consul`.

Listed below are all the properties available for bridge configuration:
```yaml
kumuluzee:
  reactive:
    vertx:
      discovery:
        env: 
          name: dev
        ttl: 30
        ping-interval: 20
```

* `ping-interval`: an interval in which service updates registration key value in the store. Default value is 20.
* `ttl`: time to live of a registration key in the store. Default value is 30 seconds.
* `env.name`: environment in which service is registered. If not provided `kumuluzee.env.name` is used, which has 
a default value of `dev`.

### How it works

KumuluzEE Reactive Vert.x extension forms a cluster with other Vert.x instances, thus enabling the capturing of events
(e.g. registration of a service). Knowing that, we must start a microservice using `kumuluzee.reactive.vertx.cluster`
set to `true`. The following image illustrates how the bridge works.

![KumuluzEE Reactive Vert.x Service Discovery](https://raw.githubusercontent.com/kumuluz/kumuluzee-reactive/master/resources/img/service_discovery.png)

### Registering a service

When registering a service in Vert.x Service Discovery you can override the above parameters by adding additional metadata
to a `Record`.

In the example below we will set `ttl` to 20, `ping-interval` to 15, `env` to `vertx` and `version` to `1.1.0`.
* `version`: version of service to be registered. Default value is `1.0.0`.
```java
Record record = HttpEndpoint.createRecord("some-rest-api", "localhost", 8080, "/");
record.setMetadata(new JsonObject().put("ttl", 20).put("ping-interval", 15)
        .put("env", "vertx").put("version", "1.1.0"));
```

### Requesting a service

To request for a service within a Vert.x instance, send a JSON, describing a service you want to retrieve, to
an address `vertx.discovery.request` on the eventbus.

In the example below we request for a service `customers-service` with version `1.0.0` located in `dev`
environment.
```java
JsonObject service = new JsonObject().put("name", "customer-service")
    .put("version", "1.0.0")
    .put("env", "dev");
 
vertx.eventBus().send("vertx.discovery.request", service, ar -> {
    if (ar.succeeded()) {
        JsonObject reply = (JsonObject) ar.result().body();
        // ...
    } else {
        // Handle error
    }
});
```

## Advanced options

If the annotations are not enough KumuluzEE Reactive provides util classes which consist of helper methods for taking advantage of additional features offered by Vert.x.

Example of getting a reference to Vert.x instance:
```java
Vertx vertx = VertxUtils.getInstance().getVertx();
// ...
```

Example of getting a reference to Service Discovery instance:
```java
ServiceDiscovery discovery = VertxUtils.getInstance().getServiceDiscovery();
// ...
```

## Changelog

Recent changes can be viewed on Github on the [Releases Page](https://github.com/kumuluz/kumuluzee-reactive/releases)

## Contribute

See the [contributing docs](https://github.com/kumuluz/kumuluzee-reactive/blob/master/CONTRIBUTING.md)

When submitting an issue, please follow the 
[guidelines](https://github.com/kumuluz/kumuluzee-reactive/blob/master/CONTRIBUTING.md#bugs).

When submitting a bugfix, write a test that exposes the bug and fails before applying your fix. Submit the test 
alongside the fix.

When submitting a new feature, add tests that cover the feature.

## License

MIT
