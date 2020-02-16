/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.routing.client.spring;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import io.rsocket.routing.client.RoutingClient;
import io.rsocket.routing.common.WellKnownKey;
import io.rsocket.routing.config.RoutingClientProperties;
import io.rsocket.routing.config.RoutingClientProperties.Broker;
import io.rsocket.routing.frames.Address;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import org.springframework.messaging.rsocket.RSocketRequester;

public class SpringRoutingClient implements RoutingClient, Disposable {

	private final RoutingClientProperties properties;

	private final RSocketRequester.Builder builder;

	private final MonoProcessor<Void> onClose;

	public SpringRoutingClient(RoutingClientProperties properties, RSocketRequester.Builder builder) {
		this.properties = properties;
		this.builder = builder;
		this.onClose = MonoProcessor.create();
	}

	/* for testing */ RoutingClientProperties getProperties() {
		return this.properties;
	}


	public RSocketRequester.Builder getRSocketRequesterBuilder() {
		return this.builder;
	}

	public Mono<RSocketRequester> connect() {
		return connect(builder);
	}

	public Mono<RSocketRequester> connect(RSocketRequester.Builder requesterBuilder) {
		// TODO: loadbalance?
		Broker broker = properties.getBrokers().iterator().next();
		switch (broker.getTransport()) {
		case WEBSOCKET:
			return requesterBuilder.connectWebSocket(URI.create("")); //FIXME: broker.getWsUri());
		}
		return requesterBuilder.connectTcp(broker.getHost(), broker.getPort());
	}

	public Consumer<RSocketRequester.MetadataSpec<?>> address(String destServiceName) {
		return spec -> {
			Address address = Address.from(properties.getRouteId())
					.with(WellKnownKey.SERVICE_NAME, destServiceName).build();
			spec.metadata(address, MimeTypes.ROUTING_FRAME_MIME_TYPE);
		};
	}

	public Consumer<RSocketRequester.MetadataSpec<?>> address(
			Consumer<Address.Builder> builderConsumer) {
		return spec -> {
			Address.Builder builder = Address.from(properties.getRouteId());
			builderConsumer.accept(builder);
			spec.metadata(builder.build(), MimeTypes.ROUTING_FRAME_MIME_TYPE);
		};
	}

	public Mono<Void> onClose() {
		return onClose;
	}

	@Override
	public boolean isDisposed() {
		return onClose.isTerminated();
	}

	@Override
	public void dispose() {
		onClose.onComplete();
	}

	@Override
	public void close() throws IOException {

	}
}
