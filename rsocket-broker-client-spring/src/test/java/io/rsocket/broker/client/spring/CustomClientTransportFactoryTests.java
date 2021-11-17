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

package io.rsocket.broker.client.spring;

import java.net.URI;

import io.rsocket.core.RSocketServer;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.spring.ClientTransportFactory;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.local.LocalClientTransport;
import io.rsocket.transport.local.LocalServerTransport;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.RouteMatcher;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"io.rsocket.broker.client.route-id=00000000-0000-0000-0000-000000000008",
		"io.rsocket.broker.client.brokers[0]=local://mylocal?arg1=val1"})
public class CustomClientTransportFactoryTests {

	@Autowired
	BrokerClientProperties properties;

	@Autowired
	TestBrokerRSocketRequesterBuilder requesterBuilder;

	@Test
	public void customPropertiesAreSet() {
		assertThat(properties.getRouteId()).isEqualTo(Id.from("00000000-0000-0000-0000-000000000008"));
		assertThat(properties.getBrokers()).hasSize(1);
		URI broker = properties.getBrokers().get(0);
		assertThat(broker).isNotNull().hasScheme("local").hasHost("mylocal").hasParameter("arg1", "val1");

		assertThat(requesterBuilder.localTransportSet).isTrue();
	}

	private static class TestBrokerRSocketRequesterBuilder extends BrokerRSocketRequesterBuilder {

		boolean localTransportSet = false;

		public TestBrokerRSocketRequesterBuilder(RSocketRequester.Builder delegate, BrokerClientProperties properties, RouteMatcher routeMatcher) {
			super(delegate, properties, routeMatcher);
		}

		@Override
		public BrokerRSocketRequester transport(ClientTransport transport) {
			if (transport instanceof LocalClientTransport) {
				localTransportSet = true;
			}
			return super.transport(transport);
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	protected static class TestConfig {

		@Bean
		@Order(Ordered.HIGHEST_PRECEDENCE)
		ClientTransportFactory customClientTransportFactory() {
			// so LocalClientTransport doesn't fail
			RSocketServer.create().bind(LocalServerTransport.create("mylocal")).subscribe();
			return new ClientTransportFactory() {
				@Override
				public boolean supports(URI uri) {
					return uri.getScheme().equalsIgnoreCase("local");
				}

				@Override
				public ClientTransport create(URI uri) {
					return LocalClientTransport.create(uri.getHost());
				}
			};
		}

		@Bean
		BrokerRSocketRequesterBuilder testBrokerRSocketRequesterBuilder(RSocketStrategies strategies,
				BrokerClientProperties properties) {
			return new TestBrokerRSocketRequesterBuilder(RSocketRequester.builder(), properties, strategies.routeMatcher());
		}

	}
}
