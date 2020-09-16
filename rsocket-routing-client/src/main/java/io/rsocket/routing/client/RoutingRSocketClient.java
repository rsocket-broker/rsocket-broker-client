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

package io.rsocket.routing.client;

import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.routing.common.Tags;
import io.rsocket.routing.common.WellKnownKey;
import io.rsocket.routing.frames.AddressFlyweight;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RoutingRSocketClient implements RSocketClient, Route {

	private final RoutingRSocketConnector connector;
	private final RSocketClient delegate;

	public RoutingRSocketClient(RoutingRSocketConnector connector, RSocketClient delegate) {
		this.connector = connector;
		this.delegate = delegate;
	}

	@Override
	public void encodeAddressMetadata(CompositeByteBuf metadataHolder, String serviceName) {
		Tags tags = Tags.builder().with(WellKnownKey.SERVICE_NAME, serviceName)
				.buildTags();
		ByteBuf address = AddressFlyweight
				.encode(connector.getAllocator(), connector.getRouteId(), Tags.empty(), tags);
		encodeAndAddMetadata(metadataHolder, address, MimeTypes.ROUTING_FRAME_MIME_TYPE);
	}

	@Override
	public void encodeAddressMetadata(CompositeByteBuf metadataHolder, Consumer<Tags.Builder<?>> tagsConsumer) {
		Tags.Builder<?> builder = Tags.builder();
		tagsConsumer.accept(builder);
		ByteBuf address = AddressFlyweight
				.encode(connector.getAllocator(), connector.getRouteId(), Tags.empty(), builder.buildTags());
		encodeAndAddMetadata(metadataHolder, address, MimeTypes.ROUTING_FRAME_MIME_TYPE);
	}

	@Override
	public ByteBufAllocator allocator() {
		return connector.getAllocator();
	}

	private void encodeAndAddMetadata(CompositeByteBuf composite, ByteBuf byteBuf, String mimeType) {
		CompositeMetadataCodec.encodeAndAddMetadata(composite, connector.getAllocator(), mimeType, byteBuf);
	}

	@Override
	public Mono<RSocket> source() {
		return delegate.source();
	}

	@Override
	public Mono<Void> fireAndForget(Mono<Payload> payloadMono) {
		return delegate.fireAndForget(payloadMono);
	}

	@Override
	public Mono<Payload> requestResponse(Mono<Payload> payloadMono) {
		return delegate.requestResponse(payloadMono);
	}

	@Override
	public Flux<Payload> requestStream(Mono<Payload> payloadMono) {
		return delegate.requestStream(payloadMono);
	}

	@Override
	public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
		return delegate.requestChannel(payloads);
	}

	@Override
	public Mono<Void> metadataPush(Mono<Payload> payloadMono) {
		return delegate.metadataPush(payloadMono);
	}

	@Override
	public void dispose() {
		delegate.dispose();
	}

	@Override
	public boolean isDisposed() {
		return delegate.isDisposed();
	}
}
