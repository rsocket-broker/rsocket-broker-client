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

package io.rsocket.broker.common.spring;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.broker.frames.Address;
import io.rsocket.broker.frames.BrokerInfo;
import io.rsocket.broker.frames.FrameHeaderFlyweight;
import io.rsocket.broker.frames.FrameType;
import io.rsocket.broker.frames.RouteJoin;
import io.rsocket.broker.frames.RouteRemove;
import io.rsocket.broker.frames.RouteSetup;
import io.rsocket.broker.frames.BrokerFrame;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.util.MimeType;


public class BrokerFrameDecoder extends AbstractDecoder<BrokerFrame> {

	public BrokerFrameDecoder() {
		super(MimeTypes.BROKER_FRAME_MIME_TYPE);
	}

	@Override
	public Flux<BrokerFrame> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
		return Flux.from(inputStream)
				.flatMap(dataBuffer -> {
					BrokerFrame frame = decode(dataBuffer, elementType, mimeType, hints);
					if (frame == null) {
						return Mono.empty();
					}
					return Mono.just(frame);
				});
	}

	@Override
	public BrokerFrame decode(DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
		try {
			ByteBuf byteBuf = asByteBuf(buffer);
			// FIXME hack for ClusterJoinListener.setupRSocket() in broker broker
			if (!byteBuf.isReadable()) {
				return new BrokerFrame(FrameType.RESERVED, 0) {
				};
			}
			int flags = FrameHeaderFlyweight.flags(byteBuf);
			FrameType frameType = FrameHeaderFlyweight.frameType(byteBuf);
			switch (frameType) {
			case ADDRESS:
				return Address.from(byteBuf, flags);
			case BROKER_INFO:
				return BrokerInfo.from(byteBuf);
			case ROUTE_JOIN:
				return RouteJoin.from(byteBuf);
			case ROUTE_REMOVE:
				return RouteRemove.from(byteBuf);
			case ROUTE_SETUP:
				return RouteSetup.from(byteBuf);
			}
			throw new IllegalArgumentException("Unknown FrameType " + frameType);
		}
		finally {
			DataBufferUtils.release(buffer);
		}
	}

	private static ByteBuf asByteBuf(DataBuffer buffer) {
		return buffer instanceof NettyDataBuffer
				? ((NettyDataBuffer) buffer).getNativeBuffer()
				: Unpooled.wrappedBuffer(buffer.asByteBuffer());
	}
}
