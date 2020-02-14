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

import java.util.LinkedHashMap;
import java.util.Map;

import io.rsocket.routing.common.MutableKey;
import io.rsocket.routing.config.RoutingClientProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;

import static io.rsocket.routing.config.RoutingClientProperties.CONFIG_PREFIX;

@ConfigurationProperties(CONFIG_PREFIX + ".address")
public class SpringRoutingClientProperties extends RoutingClientProperties {

	private Map<String, Map<MutableKey, String>> address = new LinkedHashMap<>();

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

}
