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

package io.rsocket.routing.client.spring;

import java.net.URI;
import java.util.function.Consumer;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import reactor.core.publisher.Mono;

import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import org.springframework.util.RouteMatcher;

final class RoutingRSocketRequesterBuilder implements RSocketRequester.Builder {

	private final RSocketRequester.Builder delegate;

	private final RoutingClientProperties properties;

	private final RouteMatcher routeMatcher;

	RoutingRSocketRequesterBuilder(RSocketRequester.Builder delegate,
			RoutingClientProperties properties, RouteMatcher routeMatcher) {
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
	public RoutingRSocketRequester tcp(String host, int port) {
		return wrap(delegate.tcp(host, port));
	}

	@Override
	public RoutingRSocketRequester websocket(URI uri) {
		return wrap(delegate.websocket(uri));
	}

	@Override
	public RoutingRSocketRequester transport(ClientTransport transport) {
		return wrap(delegate.transport(transport));
	}

	@Override
	public RSocketRequester.Builder apply(Consumer<RSocketRequester.Builder> configurer) {
		return delegate.apply(configurer);
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

	private RoutingRSocketRequester wrap(RSocketRequester requester) {
		return new RoutingRSocketRequester(requester, properties, routeMatcher);
	}

}
