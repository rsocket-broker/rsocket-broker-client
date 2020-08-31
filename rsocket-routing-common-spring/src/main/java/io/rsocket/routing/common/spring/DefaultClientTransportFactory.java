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

package io.rsocket.routing.common.spring;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;

public class DefaultClientTransportFactory implements ClientTransportFactory {

	@Override
	public boolean supports(TransportProperties properties) {
		return properties.getWebsocket() != null || properties.getTcp() != null;
	}

	@Override
	public ClientTransport create(TransportProperties properties) {
		// order of precedence, websocket, tcp
		if (properties.getWebsocket() != null) {
			return WebsocketClientTransport.create(properties.getWebsocket().getUri());
		}
		else if (properties.getTcp() != null) {
			return TcpClientTransport.create(properties.getTcp().getHost(), properties.getTcp().getPort());
		}
		throw new IllegalArgumentException("No valid Transport configured " + properties);
	}
}
