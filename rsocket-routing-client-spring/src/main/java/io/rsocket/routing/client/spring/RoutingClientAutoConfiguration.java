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

import io.rsocket.RSocket;
import io.rsocket.routing.config.RoutingClientProperties;
import io.rsocket.routing.frames.RouteSetup;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import static io.rsocket.routing.config.RoutingClientProperties.CONFIG_PREFIX;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({ RSocket.class, RSocketRequester.class })
@AutoConfigureAfter(BrokerRSocketStrategiesAutoConfiguration.class)
@AutoConfigureBefore(RSocketRequesterAutoConfiguration.class)
public class RoutingClientAutoConfiguration {

	@Bean
	@ConfigurationProperties(CONFIG_PREFIX)
	public RoutingClientProperties routingClientConfiguration() {
		return new SpringRoutingClientProperties();
	}

	@Bean
	@Scope("prototype") // TODO: I don't think prototype works here
	@ConditionalOnMissingBean
	public RSocketRequester.Builder routingClientRSocketRequesterBuilder(
			RSocketMessageHandler messageHandler, RSocketStrategies strategies,
			SpringRoutingClientProperties properties) {
		RouteSetup.Builder routeSetup = RouteSetup.from(properties.getRouteId(),
				properties.getServiceName());
		properties.getTags().forEach((key, value) -> {
			if (key.getWellKnownKey() != null) {
				routeSetup.with(key.getWellKnownKey(), value);
			}
			else if (key.getKey() != null) {
				routeSetup.with(key.getKey(), value);
			}
		});

		//MicrometerRSocketInterceptor interceptor = new MicrometerRSocketInterceptor(
		//		meterRegistry, Tag.of("servicename", properties.getServiceName()));

		RSocketRequester.Builder builder = RSocketRequester.builder()
				.setupMetadata(routeSetup.build(), MimeTypes.ROUTING_FRAME_MIME_TYPE)
				.rsocketStrategies(strategies).rsocketConnector(configurer(messageHandler));

		return new ClientRSocketRequesterBuilder(builder, properties,
				strategies.routeMatcher());

	}


	private RSocketConnectorConfigurer configurer(RSocketMessageHandler messageHandler) {
		return rsocketFactory -> rsocketFactory //.addRequesterPlugin(interceptor)
				.acceptor(messageHandler.responder());
	}

	@Bean
	public SpringRoutingClient springRoutingClient(RoutingClientProperties config,
			RSocketRequester.Builder builder) {
		return new SpringRoutingClient(config, builder);
	}


	@Bean
	@ConditionalOnProperty(name = CONFIG_PREFIX + ".auto-connect", matchIfMissing = true)
	public BrokerClientConnectionListener brokerClientConnectionListener(
			SpringRoutingClient client, ApplicationEventPublisher publisher) {
		return new BrokerClientConnectionListener(client, publisher);
	}

}
