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

package io.rsocket.broker.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.MimeTypes;
import io.rsocket.broker.common.Tags;
import io.rsocket.broker.frames.RouteSetupFlyweight;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class BrokerRSocketConnector {

	private RSocketConnector delegate;
	private Id routeId;
	private String serviceName;
	private List<Tuple2<ByteBuf, String>> setupMetadatas = new ArrayList<>();
	private Tags tags;
	private ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
	private ByteBuf setupData = Unpooled.EMPTY_BUFFER;

	private BrokerRSocketConnector() {
		this(RSocketConnector.create().payloadDecoder(PayloadDecoder.ZERO_COPY)
				.metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString()));
	}

	private BrokerRSocketConnector(RSocketConnector rSocketConnector) {
		delegate = rSocketConnector;
	}

	Id getRouteId() {
		return this.routeId;
	}

	ByteBufAllocator getAllocator() {
		return this.allocator;
	}

	/**
	 * Static factory method to create an {@link BrokerRSocketConnector} instance and customize default
	 * settings before connecting.
	 * @return the {@link BrokerRSocketConnector} created.
	 */
	public static BrokerRSocketConnector create() {
		return new BrokerRSocketConnector();
	}

	public static BrokerRSocketConnector create(RSocketConnector connector) {
		return new BrokerRSocketConnector(connector);
	}

	public BrokerRSocketConnector configure(Consumer<RSocketConnector> consumer) {
		consumer.accept(delegate);
		return this;
	}

	public BrokerRSocketConnector byteBufAllocator(ByteBufAllocator allocator) {
		this.allocator = allocator;
		return this;
	}

	public BrokerRSocketConnector routeId(Id routeId) {
		this.routeId = routeId;
		return this;
	}

	public BrokerRSocketConnector serviceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}

	public BrokerRSocketConnector setupTags(Tags tags) {
		this.tags = tags;
		return this;
	}

	public BrokerRSocketConnector setupMetadata(ByteBuf byteBuf, String mimeType) {
		setupMetadatas.add(Tuples.of(byteBuf, mimeType));
		return this;
	}

	public BrokerRSocketConnector setupData(ByteBuf byteBuf) {
		this.setupData = byteBuf;
		return this;
	}

	public Mono<RSocket> connect(ClientTransport transport) {
		createSetupPayload();
		return delegate.connect(transport);
	}

	public Mono<RSocket> connect(Supplier<ClientTransport> transportSupplier) {
		createSetupPayload();
		return delegate.connect(transportSupplier);
	}

	public BrokerRSocketClient toRSocketClient(ClientTransport transport) {
		createSetupPayload();
		return new BrokerRSocketClient(this, RSocketClient.from(delegate.connect(transport)));
	}

	private void createSetupPayload() {
		Tags setupTags = tags;
		if (setupTags == null) {
			setupTags = Tags.empty();
		}

		Id setupRouteId = routeId;
		if (setupRouteId == null) {
			setupRouteId = Id.random();
		}

		if (serviceName == null || serviceName.isEmpty()) {
			throw new IllegalArgumentException("serviceName must not be null or empty");
		}

		ByteBuf routeSetup = RouteSetupFlyweight.encode(allocator, setupRouteId, serviceName, setupTags, 0);
		CompositeByteBuf setupMetadata = allocator.compositeBuffer();
		CompositeMetadataCodec
				.encodeAndAddMetadata(setupMetadata, allocator, MimeTypes.BROKER_FRAME_MIME_TYPE.toString(), routeSetup);
		setupMetadatas.forEach(entry -> CompositeMetadataCodec
				.encodeAndAddMetadata(setupMetadata, allocator, entry.getT2(), entry.getT1()));

		Payload setupPayload = DefaultPayload.create(setupData, setupMetadata);

		delegate.setupPayload(setupPayload);
	}

}
