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

import java.net.URI;

import io.rsocket.RSocket;
import io.rsocket.routing.config.RoutingClientProperties;
import io.rsocket.routing.frames.RouteSetup;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.CollectionUtils;

import static io.rsocket.routing.config.RoutingClientProperties.CONFIG_PREFIX;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({RSocket.class, RSocketRequester.class})
@ConditionalOnProperty(name = CONFIG_PREFIX + ".enabled", matchIfMissing = true)
@AutoConfigureAfter(ClientRSocketStrategiesAutoConfiguration.class)
@AutoConfigureBefore(RSocketRequesterAutoConfiguration.class)
public class RoutingClientAutoConfiguration {

	@Bean
	public SpringRoutingClientProperties springRoutingClientProperties() {
		return new SpringRoutingClientProperties();
	}

	@Bean
	@Scope("prototype") // TODO: I don't think prototype works here
	@ConditionalOnMissingBean
	public RSocketRequester.Builder routingClientRSocketRequesterBuilder(
			RSocketConnectorConfigurer configurer, RSocketStrategies strategies,
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
				.rsocketStrategies(strategies).rsocketConnector(configurer);

		//TODO: RSocketRequesterBuilderCustomizer

		return new ClientRSocketRequesterBuilder(builder, properties,
				strategies.routeMatcher());

	}

	@Bean
	@ConditionalOnMissingBean
	public RSocketConnectorConfigurer rSocketConnectorConfigurer(RSocketMessageHandler messageHandler) {
		return connector -> connector //.addRequesterPlugin(interceptor)
				.acceptor(messageHandler.responder());
	}

	@Bean
	public RoutingMetadata routingMetadata(RoutingClientProperties config) {
		return new RoutingMetadata(config);
	}

	@Bean
	@ConditionalOnProperty(name = CONFIG_PREFIX + ".block", matchIfMissing = true)
	public ClientThreadManager clientThreadManager() {
		return new ClientThreadManager();
	}

	@Bean
	@ConditionalOnProperty(name = CONFIG_PREFIX + ".auto-connect", matchIfMissing = true)
	public RSocketRequester brokerClientRSocketRequester(RSocketRequester.Builder builder,
			RoutingClientProperties properties, ClientThreadManager ignored) {
		if (CollectionUtils.isEmpty(properties.getBrokers())) {
			throw new IllegalStateException(CONFIG_PREFIX + ".brokers may not be empty");
		}
		// TODO: use loadbalancer https://github.com/rsocket-routing/rsocket-routing-client/issues/8
		RoutingClientProperties.Broker broker = properties.getBrokers().iterator().next();
		RSocketRequester requester;

		switch (broker.getTransport()) {
		case WEBSOCKET:
			//FIXME: allow setting path and scheme
			requester = builder.websocket(URI.create("ws://" + broker.getHost() + ":" + broker.getPort()));
			break;
		default:
			// TODO: custom ClientTransport https://github.com/rsocket-routing/rsocket-routing-client/issues/9
			requester = builder.tcp(broker.getHost(), broker.getPort());
		}

		// if we don't subscribe, there won't be a connection to the broker.
		requester.rsocketClient().source().subscribe();

		return requester;
	}

	private class ClientThreadManager implements Disposable {

		private One<Void> onClose = Sinks.one();

		private ClientThreadManager() {
			Thread awaitThread = new Thread("routing-client-thread") {
				@Override
				public void run() {
					onClose.asMono().block();
				}
			};
			awaitThread.setContextClassLoader(getClass().getClassLoader());
			awaitThread.setDaemon(false);
			awaitThread.start();
		}

		@Override
		public void dispose() {
			onClose.emitEmpty();
		}

		@Override
		public boolean isDisposed() {
			return false;
		}

	}
}
