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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.MutableKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.MimeType;

import static io.rsocket.broker.client.spring.BrokerClientProperties.CONFIG_PREFIX;

@ConfigurationProperties(CONFIG_PREFIX)
public class BrokerClientProperties {

	public static final String CONFIG_PREFIX = "io.rsocket.broker.client";

	private boolean enabled;

	private Id routeId = Id.random();

	private String serviceName;

	private Map<MutableKey, String> tags = new LinkedHashMap<>();

	private List<URI> brokers = new ArrayList<>();

	private Map<String, Map<MutableKey, String>> address = new LinkedHashMap<>();

	private boolean failIfMissingBrokerMetadata = true;

	private MimeType dataMimeType;

	public BrokerClientProperties() {
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

	public List<URI> getBrokers() {
		return this.brokers;
	}

	public void setBrokers(List<URI> brokers) {
		this.brokers = brokers;
	}

	public Map<String, Map<MutableKey, String>> getAddress() {
		return address;
	}

	public boolean isFailIfMissingBrokerMetadata() {
		return this.failIfMissingBrokerMetadata;
	}

	public void setFailIfMissingBrokerMetadata(boolean failIfMissingBrokerMetadata) {
		this.failIfMissingBrokerMetadata = failIfMissingBrokerMetadata;
	}

	public MimeType getDataMimeType() {
		return dataMimeType;
	}

	public void setDataMimeType(MimeType dataMimeType) {
		this.dataMimeType = dataMimeType;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringCreator(this)
				.append("enabled", isEnabled())
				.append("routeId", getRouteId())
				.append("serviceName", getServiceName())
				.append("tags", getTags())
				.append("broker", getBrokers())
				.append("address", address)
				.append("failIfMissingBrokerMetadata", failIfMissingBrokerMetadata)
				.append("dataMimeType", dataMimeType)
				.toString();
		// @formatter:on
	}

}
