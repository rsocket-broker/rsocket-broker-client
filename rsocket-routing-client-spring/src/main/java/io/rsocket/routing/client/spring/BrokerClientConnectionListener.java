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

import java.util.function.Consumer;

import reactor.core.publisher.Mono;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.messaging.rsocket.RSocketRequester;

/**
 * Automatically subscribes to {@link SpringRoutingClient}. On subscribe it publishes a
 * {@link PayloadApplicationEvent} with a generic type of {@link RSocketRequester}.
 */
public class BrokerClientConnectionListener
		implements ApplicationListener<ApplicationReadyEvent>, Ordered {

	private final SpringRoutingClient routingClient;

	private final ApplicationEventPublisher publisher;

	public BrokerClientConnectionListener(SpringRoutingClient routingClient,
			ApplicationEventPublisher publisher) {
		this.routingClient = routingClient;
		this.publisher = publisher;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		// TODO: is there a better event the just RSocketRequester?
		// TODO: save Disposable?
		startAwaitThread();
		Mono<RSocketRequester> requesterMono = this.routingClient.connect();
		requesterMono.subscribe(publishEvent());
	}

	private void startAwaitThread() {
		Thread awaitThread = new Thread("routing-client-thread") {
			@Override
			public void run() {
				routingClient.onClose().block();
			}
		};
		awaitThread.setContextClassLoader(getClass().getClassLoader());
		awaitThread.setDaemon(false);
		awaitThread.start();
	}

	private Consumer<RSocketRequester> publishEvent() {
		return requester -> {
			publisher.publishEvent(new RSocketRequesterEvent<>(
					BrokerClientConnectionListener.this, requester));
		};
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE; // TODO: configurable
	}

	private static final class RSocketRequesterEvent<T extends RSocketRequester>
			extends PayloadApplicationEvent<T> {

		private RSocketRequesterEvent(Object source, T payload) {
			super(source, payload);
		}

		@Override
		public ResolvableType getResolvableType() {
			return ResolvableType.forClassWithGenerics(getClass(),
					ResolvableType.forClass(RSocketRequester.class));
		}

	}

}
