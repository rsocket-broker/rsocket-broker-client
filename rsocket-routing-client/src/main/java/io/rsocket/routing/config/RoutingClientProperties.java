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

package io.rsocket.routing.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.MutableKey;
import io.rsocket.routing.common.Transport;

public class RoutingClientProperties {

	public static final String CONFIG_PREFIX = "io.rsocket.routing.client";

	private Id routeId = Id.random();

	private String serviceName;

	private Map<MutableKey, String> tags = new LinkedHashMap<>();

	private List<Broker> brokers = new ArrayList<>();

	public RoutingClientProperties() {
	}

	public Id getRouteId() {
		return this.routeId;
	}

	public void setRouteId(Id routeId) {
		this.routeId = routeId;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Map<MutableKey, String> getTags() {
		return this.tags;
	}

	public void setTags(Map<MutableKey, String> tags) {
		this.tags = tags;
	}

	public List<Broker> getBrokers() {
		return this.brokers;
	}

	public void setBrokers(List<Broker> brokers) {
		this.brokers = brokers;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", RoutingClientProperties.class
				.getSimpleName() + "[", "]")
				.add("routeId=" + routeId)
				.add("serviceName='" + serviceName + "'")
				.add("tags=" + tags)
				.add("brokers=" + brokers)
				.toString();
	}

	public static class Broker {
		private String host;

		private int port;

		private Transport transport = Transport.TCP;

		public String getHost() {
			return this.host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public Transport getTransport() {
			return this.transport;
		}

		public void setTransport(Transport transport) {
			this.transport = transport;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
					.add("host='" + host + "'")
					.add("port=" + port)
					.add("port=" + transport)
					.toString();
		}
	}

}
