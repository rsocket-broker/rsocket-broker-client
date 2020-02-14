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

package io.rsocket.routing.client;

import java.io.IOException;

import io.rsocket.routing.config.RoutingClientProperties;
import io.rsocket.routing.config.TypesafeConfigFactory;

public class DefaultRoutingClient implements RoutingClient {

	private final RoutingClientProperties config;

	public DefaultRoutingClient() {
		this(TypesafeConfigFactory.load());
	}

	protected DefaultRoutingClient(RoutingClientProperties config) {
		this.config = config;
	}

	/* for testing */ RoutingClientProperties getConfig() {
		return this.config;
	}

	@Override
	public void close() throws IOException {

	}
}
