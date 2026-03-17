# RSocket Broker Client

[![Gitter](https://badges.gitter.im/rsocket-routing/community.svg)](https://gitter.im/rsocket-routing/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Client library for connecting to an [RSocket Broker](https://github.com/rsocket-broker/rsocket-broker). Implements the binary frame protocol defined in the [RSocket Broker Specification](https://github.com/rsocket-broker/rsocket-broker-spec), providing frame encoding/decoding, connection setup, and address-based request routing.

## Overview

This library enables RSocket clients to:

- **Register as a routable destination** by sending a `ROUTE_SETUP` frame with a service name and tags
- **Route requests through the broker** by attaching `ADDRESS` metadata with tags that identify the target service
- **Support all RSocket interaction models** — fire-and-forget, request/response, request/stream, request/channel, and metadata push
- **Choose routing strategies** — Unicast, Multicast, or Shard routing via ADDRESS flags

## Modules

| Module                         | Description                                                                                                                                                 |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `rsocket-broker-common`        | Shared types: `Id` (128-bit UUID identifier), `Tags` (immutable key-value map), `Key`, `WellKnownKey` (spec-defined tag keys), `MimeTypes`, and `Transport` |
| `rsocket-broker-frames`        | Binary frame codec: flyweight encoders/decoders for `ROUTE_SETUP`, `ADDRESS`, `BROKER_INFO`, `ROUTE_JOIN`, and `ROUTE_REMOVE` frames per the specification  |
| `rsocket-broker-client`        | Core client: `BrokerRSocketConnector` (connection builder) and `BrokerRSocketClient` (RSocketClient wrapper with address encoding)                          |
| `rsocket-broker-common-spring` | Spring integration for common types: `BrokerFrameEncoder`, `BrokerFrameDecoder`, `ClientTransportFactory`                                                   |
| `rsocket-broker-client-spring` | Spring Boot auto-configuration: `BrokerRSocketRequester`, `BrokerClientProperties`, automatic broker connection                                             |

## Frame Types

The library implements the following frame types from the [RSocket Broker Specification](https://github.com/rsocket-broker/rsocket-broker-spec):

| Frame          | Code   | Purpose                                                                                                                                                          |
| -------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ROUTE_SETUP`  | `0x01` | Sent by a client to register as a routable destination with a service name and tags                                                                              |
| `ROUTE_JOIN`   | `0x02` | Sent between brokers when a routable destination connects                                                                                                        |
| `ROUTE_REMOVE` | `0x03` | Sent between brokers when a routable destination disconnects                                                                                                     |
| `BROKER_INFO`  | `0x04` | Exchanged between brokers to establish forwarding relationships                                                                                                  |
| `ADDRESS`      | `0x05` | Attached to request metadata to route messages to a destination. Contains origin route ID, routing tags, metadata, and flags (unicast/multicast/shard/encrypted) |

All frames share a common 6-byte header with major/minor version and frame type + flags. The MIME type for broker frames is `message/x.rsocket.broker.frame.v0`.

## Well-Known Keys

The specification defines well-known tag keys for compact encoding. Key examples:

| Key                             | ID     | Description                           |
| ------------------------------- | ------ | ------------------------------------- |
| `io.rsocket.broker.ServiceName` | `0x01` | Service name tag                      |
| `io.rsocket.broker.RouteId`     | `0x02` | Route identifier tag                  |
| `io.rsocket.broker.Region`      | `0x06` | Region tag (e.g., AWS region)         |
| `io.rsocket.broker.Environment` | `0x10` | Environment tag (test, staging, prod) |
| `io.rsocket.broker.LBMethod`    | `0x1E` | Load balancing algorithm hint         |
| `io.rsocket.broker.ShardKey`    | `0x1B` | Shard key for shard routing           |

See `WellKnownKey` for the full list of 30+ well-known keys.

## Getting Started

### Prerequisites

- Java 21+

### Core Client (No Spring)

Add the dependency:

```groovy
implementation 'io.rsocket.broker:rsocket-broker-client:0.4.0-SNAPSHOT'
```

Connect to a broker and send requests:

```java
// Create a connector with route setup
BrokerRSocketClient client = BrokerRSocketConnector.create()
    .serviceName("my-service")
    .routeId(Id.random())
    .setupTags(Tags.builder()
        .with(WellKnownKey.REGION, "us-east-1")
        .buildTags())
    .toRSocketClient(TcpClientTransport.create("localhost", 8001));

// Send a request with address metadata
CompositeByteBuf metadata = client.allocator().compositeBuffer();
client.encodeAddressMetadata(metadata, "target-service");

Payload payload = DefaultPayload.create("request-data", metadata);
client.requestResponse(Mono.just(payload))
    .subscribe(response -> System.out.println(response.getDataUtf8()));
```

### Spring Boot Client

Add the Spring Boot starter:

```groovy
implementation 'io.rsocket.broker:rsocket-broker-client-spring:0.4.0-SNAPSHOT'
```

Configure in `application.yml`:

```yaml
io:
  rsocket:
    broker:
      client:
        service-name: my-service
        brokers:
          - tcp://localhost:8001
        tags:
          REGION: us-east-1
          ENVIRONMENT: production
        address:
          "target-service": # route pattern
            SERVICE_NAME: target-service
```

Use the auto-configured `BrokerRSocketRequester`:

```java
@Autowired
BrokerRSocketRequester requester;

public Mono<String> callTargetService(String request) {
    return requester.route("target-service")
        .data(request)
        .retrieveMono(String.class);
}
```

The `BrokerRSocketRequester` automatically matches routes against the configured address patterns and attaches the appropriate `ADDRESS` metadata to outgoing requests.

### Using BrokerMetadata Directly

For more control over address construction:

```java
@Autowired
BrokerMetadata brokerMetadata;

@Autowired
RSocketRequester requester;

public Mono<String> callService() {
    return requester.route("any-route")
        .metadata(brokerMetadata.address("target-service"))
        .data("request")
        .retrieveMono(String.class);
}
```

## Configuration Properties

All properties are under the `io.rsocket.broker.client` prefix:

| Property                          | Default | Description                                                        |
| --------------------------------- | ------- | ------------------------------------------------------------------ |
| `enabled`                         | `true`  | Enable/disable the broker client                                   |
| `service-name`                    | —       | Service name sent in the `ROUTE_SETUP` frame                       |
| `route-id`                        | random  | Route ID for this client connection                                |
| `brokers`                         | —       | List of broker URIs (`tcp://`, `ws://`, or `wss://`)               |
| `tags`                            | —       | Tags to include in the `ROUTE_SETUP` frame                         |
| `address`                         | —       | Map of route patterns to address tags for automatic routing        |
| `auto-connect`                    | `true`  | Automatically connect to the first broker on startup               |
| `block`                           | `true`  | Keep a daemon thread alive to prevent the application from exiting |
| `fail-if-missing-broker-metadata` | `true`  | Fail requests that don't have broker address metadata              |
| `data-mime-type`                  | —       | Override the data MIME type for the connection                     |

## Transport Protocols

The client supports three URI schemes for broker connections:

| Scheme   | Transport       | Example             |
| -------- | --------------- | ------------------- |
| `tcp://` | Raw TCP         | `tcp://broker:8001` |
| `ws://`  | WebSocket       | `ws://broker:8001`  |
| `wss://` | WebSocket + TLS | `wss://broker:8443` |

### Configuring Secure WebSocket (wss://)

The `DefaultClientTransportFactory` uses Reactor Netty's `WebsocketClientTransport.create(URI)` which automatically negotiates TLS when the scheme is `wss://`. By default, the JVM's trust store is used to validate the server certificate.

Basic usage — just change the URI scheme:

```yaml
io:
  rsocket:
    broker:
      client:
        brokers:
          - wss://broker:8443
```

For self-signed certificates or custom trust stores, you need to provide a custom `ClientTransportFactory` bean that configures the Reactor Netty `TcpClient` with the appropriate SSL context:

```java
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
ClientTransportFactory secureWebSocketClientTransportFactory() {
    return new ClientTransportFactory() {
        @Override
        public boolean supports(URI uri) {
            return "wss".equalsIgnoreCase(uri.getScheme());
        }

        @Override
        public ClientTransport create(URI uri) {
            TcpClient tcpClient = TcpClient.create()
                .host(uri.getHost())
                .port(uri.getPort())
                .secure(ssl -> ssl.sslContext(
                    SslContextBuilder.forClient()
                        .trustManager(trustedCertFile)  // custom trust store
                        .build()
                ));
            HttpClient httpClient = HttpClient.from(tcpClient);
            return WebsocketClientTransport.create(httpClient, uri.getPath());
        }
    };
}
```

With Spring Boot SSL bundles (Spring Boot 3.1+):

```yaml
spring:
  ssl:
    bundle:
      jks:
        broker:
          truststore:
            location: classpath:broker-truststore.p12
            password: changeit
            type: PKCS12
```

```java
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
ClientTransportFactory sslBundleClientTransportFactory(SslBundles sslBundles) {
    return new ClientTransportFactory() {
        @Override
        public boolean supports(URI uri) {
            return "wss".equalsIgnoreCase(uri.getScheme());
        }

        @Override
        public ClientTransport create(URI uri) {
            SslContext sslContext = sslBundles.getBundle("broker").createSslContext();
            TcpClient tcpClient = TcpClient.create()
                .host(uri.getHost())
                .port(uri.getPort())
                .secure(ssl -> ssl.sslContext(sslContext));
            HttpClient httpClient = HttpClient.from(tcpClient);
            return WebsocketClientTransport.create(httpClient, uri.getPath());
        }
    };
}
```

> **Note:** Custom `ClientTransportFactory` beans are ordered using `@Order`. Use `Ordered.HIGHEST_PRECEDENCE` to ensure your factory is selected before the `DefaultClientTransportFactory`. The auto-configuration iterates through all registered factories and uses the first one that supports the given URI.

## Building from Source

```bash
./gradlew build
```

## Related Projects

- [rsocket-broker-spec](https://github.com/rsocket-broker/rsocket-broker-spec) — The RSocket Broker protocol specification
- [rsocket-broker](https://github.com/rsocket-broker/rsocket-broker) — The RSocket Broker server implementation
- [rsocket-java](https://github.com/rsocket/rsocket-java) — The RSocket Java implementation

## License

This project is released under the [Apache License 2.0](LICENSE.txt).
