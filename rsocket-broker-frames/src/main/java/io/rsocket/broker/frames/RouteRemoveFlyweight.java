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

import static io.rsocket.broker.frames.FlyweightUtils.decodeId;
import static io.rsocket.broker.frames.FlyweightUtils.encodeId;

/**
 * https://github.com/rsocket-broker/rsocket-broker-spec/blob/master/RSocketBrokerSpecification.md#route_remove
 */
public class RouteRemoveFlyweight {

	public static ByteBuf encode(ByteBufAllocator allocator, Id brokerId, Id routeId, long timestamp, int flags) {
		Objects.requireNonNull(brokerId, "brokerId may not be null");
		Objects.requireNonNull(routeId, "routeId may not be null");

		ByteBuf byteBuf = FrameHeaderFlyweight.encode(allocator, FrameType.ROUTE_REMOVE, flags);
		encodeId(byteBuf, brokerId);
		encodeId(byteBuf, routeId);
		byteBuf.writeLong(timestamp);

		return byteBuf;
	}

	public static Id brokerId(ByteBuf byteBuf) {
		return decodeId(byteBuf, FrameHeaderFlyweight.BYTES);
	}

	public static Id routeId(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		return decodeId(byteBuf, offset);
	}

	public static long timestamp(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES + FlyweightUtils.ID_BYTES;
		return byteBuf.getLong(offset);
	}

}
