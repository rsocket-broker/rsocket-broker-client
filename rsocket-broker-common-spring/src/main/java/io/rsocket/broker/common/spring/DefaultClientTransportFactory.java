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

package io.rsocket.broker.common.spring;

import java.net.URI;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;

public class DefaultClientTransportFactory implements ClientTransportFactory {

	@Override
	public boolean supports(URI uri) {
		return isWebsocket(uri) || isTcp(uri);
	}

	private boolean isTcp(URI uri) {
		return uri.getScheme().equalsIgnoreCase("tcp");
	}

	private boolean isWebsocket(URI uri) {
		return uri.getScheme().equalsIgnoreCase("ws") || uri.getScheme().equalsIgnoreCase("wss");
	}

	@Override
	public ClientTransport create(URI uri) {
		// order of precedence, websocket, tcp
		if (isWebsocket(uri)) {
			return WebsocketClientTransport.create(uri);
		}
		else if (isTcp(uri)) {
			return TcpClientTransport.create(uri.getHost(), uri.getPort());
		}
		throw new IllegalArgumentException("No valid Transport configured " + uri);
	}
}
