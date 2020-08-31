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

import java.util.Map;

import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.MutableKey;
import io.rsocket.routing.common.spring.TransportProperties;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT,
		properties = "io.rsocket.routing.client.auto-connect=false")
public class RoutingClientPropertiesTests {

	@Autowired
	RoutingClientProperties properties;

	@Test
	public void clientProperties() {
		assertThat(properties).isNotNull();
		assertThat(properties.getRouteId()).isEqualTo(Id.from("00000000-0000-0000-0000-000000000011"));
		assertThat(properties.getServiceName()).isEqualTo("test_requester");
		assertThat(properties.getTags()).containsEntry(new MutableKey("INSTANCE_NAME"),
				"test_requester1");
		assertThat(properties.getAddress()).containsKeys("test_responder-rc",
				"key.with.dots", "key.with.{replacement}");
		Map<MutableKey, String> map = properties.getAddress().get("test_responder-rc");
		assertThat(map).contains(entry(new MutableKey("SERVICE_NAME"), "test_responder"),
				entry(new MutableKey("custom-tag"), "custom-value"));
		assertThat(properties.getBrokers()).hasSize(1);
		TransportProperties broker = properties.getBrokers().get(0);
		assertThat(broker.getTcp()).isNotNull();
		TransportProperties.TcpProperties tcp = broker.getTcp();
		assertThat(tcp.getHost()).isEqualTo("localhost");
		assertThat(tcp.getPort()).isEqualTo(7002);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class Config {

	}

}
