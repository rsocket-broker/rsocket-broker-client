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

package io.rsocket.routing.common.spring;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.routing.frames.Address;
import io.rsocket.routing.frames.AddressFlyweight;
import io.rsocket.routing.frames.BrokerInfo;
import io.rsocket.routing.frames.BrokerInfoFlyweight;
import io.rsocket.routing.frames.RouteJoin;
import io.rsocket.routing.frames.RouteJoinFlyweight;
import io.rsocket.routing.frames.RouteRemove;
import io.rsocket.routing.frames.RouteRemoveFlyweight;
import io.rsocket.routing.frames.RouteSetup;
import io.rsocket.routing.frames.RouteSetupFlyweight;
import io.rsocket.routing.frames.RoutingFrame;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.util.MimeType;

public class RoutingFrameEncoder extends AbstractEncoder<RoutingFrame> {

	public RoutingFrameEncoder() {
		super(MimeTypes.ROUTING_FRAME_MIME_TYPE);
	}

	@Override
	public Flux<DataBuffer> encode(Publisher<? extends RoutingFrame> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
		return Flux.from(inputStream).map(value -> encodeValue(value, bufferFactory,
				elementType, mimeType, hints));
	}

	@Override
	public DataBuffer encodeValue(RoutingFrame routingFrame, DataBufferFactory bufferFactory, ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
		NettyDataBufferFactory factory = (NettyDataBufferFactory) bufferFactory;

		ByteBufAllocator allocator = factory.getByteBufAllocator();
		ByteBuf encoded;
		switch (routingFrame.getFrameType()) {
		case ADDRESS:
			Address address = (Address) routingFrame;
			encoded = AddressFlyweight.encode(allocator, address.getOriginRouteId(),
					address.getMetadata(), address.getTags(), routingFrame.getFlags());
			break;
		case BROKER_INFO:
			BrokerInfo brokerInfo = (BrokerInfo) routingFrame;
			encoded = BrokerInfoFlyweight.encode(allocator, brokerInfo.getBrokerId(),
					brokerInfo.getTimestamp(), brokerInfo.getTags(), routingFrame.getFlags());
			break;
		case ROUTE_JOIN:
			RouteJoin routeJoin = (RouteJoin) routingFrame;
			encoded = RouteJoinFlyweight.encode(allocator,
					routeJoin.getBrokerId(), routeJoin.getRouteId(), routeJoin.getTimestamp(),
					routeJoin.getServiceName(), routeJoin.getTags(), routingFrame.getFlags());
			break;
		case ROUTE_REMOVE:
			RouteRemove routeRemove = (RouteRemove) routingFrame;
			encoded = RouteRemoveFlyweight.encode(allocator,
					routeRemove.getBrokerId(), routeRemove.getRouteId(), routeRemove.getTimestamp(), routingFrame
							.getFlags());
			break;
		case ROUTE_SETUP:
			RouteSetup routeSetup = (RouteSetup) routingFrame;
			encoded = RouteSetupFlyweight.encode(allocator,
					routeSetup.getRouteId(), routeSetup.getServiceName(), routeSetup.getTags(), routingFrame
							.getFlags());
			break;
		default:
			throw new IllegalArgumentException("Unknown FrameType " + routingFrame.getFrameType());
		}

		return factory.wrap(encoded);
	}
}
