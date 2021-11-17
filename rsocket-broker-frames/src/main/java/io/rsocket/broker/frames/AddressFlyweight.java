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

package io.rsocket.broker.frames;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.Tags;

import static io.rsocket.broker.frames.FlyweightUtils.decodeId;
import static io.rsocket.broker.frames.FlyweightUtils.encodeId;

/**
 * https://github.com/rsocket-broker/rsocket-broker-spec/blob/master/RSocketBrokerSpecification.md#address
 */
public class AddressFlyweight {

	/** (I)gnore flag: a value of 0 indicates the protocol can't ignore this frame */
	public static final int FLAGS_I = 0b10_0000_0000;
	/** (E)ncrypted flag: a value of 1 indicates the payload is encrypted */
	public static final int FLAGS_E = 0b01_0000_0000;
	/** (U)unicast flag: a value of 1 indicates unicast broker */
	public static final int FLAGS_U = 0b00_1000_0000;
	/** (M)ulticast flag: a value of 1 indicates multicast broker */
	public static final int FLAGS_M = 0b00_0100_0000;
	/** (S)hard flag: a value of 1 indicates shard broker */
	public static final int FLAGS_S = 0b00_0010_0000;

	static final int ROUTING_TYPE_MASK = 0b11_0001_1111;

	public static ByteBuf encode(ByteBufAllocator allocator, Id originRouteId, Tags metadata, Tags tags, int flags) {
		Objects.requireNonNull(originRouteId, "originRouteId may not be null");
		Objects.requireNonNull(tags, "tags may not be null");

		ByteBuf byteBuf = FrameHeaderFlyweight.encode(allocator, FrameType.ADDRESS, flags);
		encodeId(byteBuf, originRouteId);

		//FIXME: how to deal with empty metadata?
		//TagsFlyweight.encode(byteBuf, metadata);

		TagsFlyweight.encode(byteBuf, tags);

		return byteBuf;
	}

	public static Id originRouteId(ByteBuf byteBuf) {
		return decodeId(byteBuf, FrameHeaderFlyweight.BYTES);
	}

	public static Tags metadata(ByteBuf byteBuf) {
		//int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		//return TagsFlyweight.decode(offset, byteBuf);
		return Tags.empty();
	}

	public static Tags tags(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		// metadata length
		//offset += TagsFlyweight.length(offset, byteBuf);
		return TagsFlyweight.decode(offset, byteBuf);
	}

}
