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
import java.util.Collections;
import java.util.List;

import io.rsocket.RSocket;
import io.rsocket.broker.common.spring.ClientTransportFactory;
import io.rsocket.broker.common.spring.DefaultClientTransportFactory;
import io.rsocket.broker.common.spring.MimeTypes;
import io.rsocket.broker.frames.RouteSetup;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
import io.rsocket.transport.ClientTransport;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;

import org.springframework.beans.factory.ObjectProvider;
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

import static io.rsocket.broker.client.spring.BrokerClientProperties.CONFIG_PREFIX;


@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({RSocket.class, RSocketRequester.class})
@ConditionalOnProperty(name = CONFIG_PREFIX + ".enabled", matchIfMissing = true)
@AutoConfigureAfter(BrokerClientRSocketStrategiesAutoConfiguration.class)
@AutoConfigureBefore(RSocketRequesterAutoConfiguration.class)
public class BrokerClientAutoConfiguration {

	//TODO: accept BrokerInfo to get updated broker nodes. Metadata Push

	@Bean
	public BrokerClientProperties brokerClientProperties() {
		return new BrokerClientProperties();
	}

	@Bean
	@Scope("prototype") // TODO: I don't think prototype works here
	@ConditionalOnMissingBean
	public BrokerRSocketRequesterBuilder brokerRSocketRequesterBuilder(
			RSocketConnectorConfigurer configurer, RSocketStrategies strategies,
			BrokerClientProperties properties) {
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
				.setupMetadata(routeSetup.build(), MimeTypes.BROKER_FRAME_MIME_TYPE)
				.rsocketStrategies(strategies).rsocketConnector(configurer);

		//TODO: BrokerRequesterBuilderCustomizer

		return new BrokerRSocketRequesterBuilder(builder, properties,
				strategies.routeMatcher());

	}

	@Bean
	@ConditionalOnMissingBean
	public RSocketConnectorConfigurer rSocketConnectorConfigurer(RSocketMessageHandler messageHandler) {
		return connector -> connector //.addRequesterPlugin(interceptor)
				.acceptor(messageHandler.responder());
	}

	@Bean
	public BrokerMetadata brokerMetadata(BrokerClientProperties config) {
		return new BrokerMetadata(config);
	}

	@Bean
	public DefaultClientTransportFactory defaultClientTransportFactory() {
		return new DefaultClientTransportFactory();
	}

	@Bean
	@ConditionalOnProperty(name = CONFIG_PREFIX + ".block", matchIfMissing = true)
	public ClientThreadManager clientThreadManager() {
		return new ClientThreadManager();
	}

	@Bean
	@ConditionalOnProperty(name = CONFIG_PREFIX + ".auto-connect", matchIfMissing = true)
	public BrokerRSocketRequester brokerClientRSocketRequester(BrokerRSocketRequesterBuilder builder,
			BrokerClientProperties properties, ObjectProvider<ClientTransportFactory> transportFactories, ClientThreadManager ignored) {
		if (CollectionUtils.isEmpty(properties.getBrokers())) {
			throw new IllegalStateException(CONFIG_PREFIX + ".brokers may not be empty");
		}
		// TODO: use loadbalancer https://github.com/rsocket-broker/rsocket-broker-client/issues/8
		URI broker = properties.getBrokers().iterator().next();

		ClientTransport clientTransport = transportFactories.orderedStream().filter(factory -> factory.supports(broker)).findFirst()
				.map(factory -> factory.create(broker))
				.orElseThrow(() -> new IllegalStateException("Unknown transport " + properties));

		// TODO: targets and strategy as beans
		Flux<List<LoadbalanceTarget>> loadbalanceTargets = Flux.just(Collections.singletonList(LoadbalanceTarget.from("config", clientTransport)));
		RoundRobinLoadbalanceStrategy loadbalanceStrategy = new RoundRobinLoadbalanceStrategy();
		BrokerRSocketRequester requester = builder.transports(loadbalanceTargets, loadbalanceStrategy);

		// if we don't subscribe, there won't be a connection to the broker.
		requester.rsocketClient().source().subscribe();

		return requester;
	}

	private static class ClientThreadManager implements Disposable {

		private final One<Void> onClose = Sinks.one();

		private ClientThreadManager() {
			Thread awaitThread = new Thread("broker-client-thread") {
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
			onClose.emitEmpty((signalType, emitResult) -> false);
		}

		@Override
		public boolean isDisposed() {
			return false;
		}

	}
}
