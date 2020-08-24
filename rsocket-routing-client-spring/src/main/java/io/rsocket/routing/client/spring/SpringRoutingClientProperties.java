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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.MutableKey;
import io.rsocket.routing.common.Transport;
import io.rsocket.routing.config.RoutingClientProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.style.ToStringCreator;

import static io.rsocket.routing.config.RoutingClientProperties.CONFIG_PREFIX;

@ConfigurationProperties(CONFIG_PREFIX)
public class SpringRoutingClientProperties implements RoutingClientProperties {

	private Id routeId = Id.random();

	private String serviceName;

	private Map<MutableKey, String> tags = new LinkedHashMap<>();

	@NestedConfigurationProperty
	private List<BrokerImpl> brokers = new ArrayList<>();

	private Map<String, Map<MutableKey, String>> address = new LinkedHashMap<>();

	public SpringRoutingClientProperties() {
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

	public List<? extends Broker> getBrokers() {
		return this.brokers;
	}

	public void setBrokers(List<BrokerImpl> brokers) {
		this.brokers = brokers;
	}

	public Map<String, Map<MutableKey, String>> getAddress() {
		return address;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringCreator(this)
				.append("routeId", getRouteId())
				.append("serviceName", getServiceName())
				.append("tags", getTags())
				.append("broker", getBrokers())
				.append("address", address)
				.toString();
		// @formatter:on
	}

	public static class BrokerImpl implements RoutingClientProperties.Broker {
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
			// @formatter:off
			return new ToStringCreator(this)
					.append("host", host)
					.append("port", port)
					.append("transport", transport)
					.toString();
			// @formatter:on
		}
	}
}
