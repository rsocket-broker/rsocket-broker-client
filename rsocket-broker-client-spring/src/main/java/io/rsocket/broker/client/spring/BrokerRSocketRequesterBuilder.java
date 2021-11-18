/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.broker.client.spring;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import io.rsocket.loadbalance.LoadbalanceStrategy;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import org.springframework.util.RouteMatcher;

public class BrokerRSocketRequesterBuilder implements RSocketRequester.Builder {

	private final RSocketRequester.Builder delegate;

	private final BrokerClientProperties properties;

	private final RouteMatcher routeMatcher;

	BrokerRSocketRequesterBuilder(RSocketRequester.Builder delegate,
			BrokerClientProperties properties, RouteMatcher routeMatcher) {
		this.delegate = delegate;
		this.properties = properties;
		this.routeMatcher = routeMatcher;
	}

	@Override
	public RSocketRequester.Builder rsocketConnector(RSocketConnectorConfigurer configurer) {
		return delegate.rsocketConnector(configurer);
	}

	@Override
	public RSocketRequester.Builder dataMimeType(MimeType mimeType) {
		return delegate.dataMimeType(mimeType);
	}

	@Override
	public RSocketRequester.Builder metadataMimeType(MimeType mimeType) {
		return delegate.metadataMimeType(mimeType);
	}

	@Override
	public RSocketRequester.Builder setupData(Object data) {
		return delegate.setupData(data);
	}

	@Override
	public RSocketRequester.Builder setupRoute(String route, Object... routeVars) {
		return delegate.setupRoute(route, routeVars);
	}

	@Override
	public RSocketRequester.Builder setupMetadata(Object value, MimeType mimeType) {
		return delegate.setupMetadata(value, mimeType);
	}

	@Override
	public RSocketRequester.Builder rsocketStrategies(RSocketStrategies strategies) {
		return delegate.rsocketStrategies(strategies);
	}

	@Override
	public RSocketRequester.Builder rsocketStrategies(
			Consumer<RSocketStrategies.Builder> configurer) {
		return delegate.rsocketStrategies(configurer);
	}

	@Override
	public BrokerRSocketRequester tcp(String host, int port) {
		return wrap(delegate.tcp(host, port));
	}

	@Override
	public BrokerRSocketRequester websocket(URI uri) {
		return wrap(delegate.websocket(uri));
	}

	@Override
	public BrokerRSocketRequester transport(ClientTransport transport) {
		return wrap(delegate.transport(transport));
	}

	@Override
	public RSocketRequester.Builder apply(Consumer<RSocketRequester.Builder> configurer) {
		return delegate.apply(configurer);
	}

	@Override
	public BrokerRSocketRequester transports(Publisher<List<LoadbalanceTarget>> targetPublisher, LoadbalanceStrategy loadbalanceStrategy) {
		return wrap(delegate.transports(targetPublisher, loadbalanceStrategy));
	}

	@Override
	@Deprecated
	public Mono<RSocketRequester> connectTcp(String host, int port) {
		return connect(TcpClientTransport.create(host, port));
	}

	@Override
	@Deprecated
	public Mono<RSocketRequester> connectWebSocket(URI uri) {
		return connect(WebsocketClientTransport.create(uri));
	}

	@Override
	@Deprecated
	public Mono<RSocketRequester> connect(ClientTransport transport) {
		return delegate.connect(transport).map(this::wrap);
	}

	private BrokerRSocketRequester wrap(RSocketRequester requester) {
		return new BrokerRSocketRequester(requester, properties, routeMatcher);
	}

}
