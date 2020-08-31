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

package io.rsocket.routing.client.spring;

import io.rsocket.core.RSocketServer;
import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.spring.ClientTransportFactory;
import io.rsocket.routing.common.spring.TransportProperties;
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
import static org.assertj.core.api.Assertions.entry;

@SpringBootTest(properties = {"io.rsocket.routing.client.route-id=00000000-0000-0000-0000-000000000008",
		"io.rsocket.routing.client.brokers[0].custom.type=local",
		"io.rsocket.routing.client.brokers[0].custom.args.name=mylocal",
		"io.rsocket.routing.client.brokers[0].cluster.custom.type=local",
		"io.rsocket.routing.client.brokers[0].cluster.custom.args.name=myclusterlocal"})
public class CustomClientTransportFactoryTests {

	@Autowired
	RoutingClientProperties properties;

	@Autowired
	TestRoutingRSocketRequesterBuilder requesterBuilder;

	@Test
	public void customPropertiesAreSet() {
		assertThat(properties.getRouteId()).isEqualTo(Id.from("00000000-0000-0000-0000-000000000008"));
		assertThat(properties.getBrokers()).hasSize(1);
		TransportProperties broker = properties.getBrokers().get(0);
		assertThat(broker.getCustom()).isNotNull();
		assertThat(broker.getCustom().getArgs()).containsOnly(entry("name", "mylocal"));

		assertThat(requesterBuilder.localTransportSet).isTrue();
	}

	private static class TestRoutingRSocketRequesterBuilder extends RoutingRSocketRequesterBuilder {

		boolean localTransportSet = false;

		public TestRoutingRSocketRequesterBuilder(RSocketRequester.Builder delegate, RoutingClientProperties properties, RouteMatcher routeMatcher) {
			super(delegate, properties, routeMatcher);
		}

		@Override
		public RoutingRSocketRequester transport(ClientTransport transport) {
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
				public boolean supports(TransportProperties properties) {
					return properties.hasCustomTransport() && properties.getCustom().getType().equals("local");
				}

				@Override
				public ClientTransport create(TransportProperties properties) {
					return LocalClientTransport.create(properties.getCustom().getArgs().get("name"));
				}
			};
		}

		@Bean
		RoutingRSocketRequesterBuilder testRoutingRSocketRequesterBuilder(RSocketStrategies strategies,
				RoutingClientProperties properties) {
			return new TestRoutingRSocketRequesterBuilder(RSocketRequester.builder(), properties, strategies.routeMatcher());
		}

	}
}
