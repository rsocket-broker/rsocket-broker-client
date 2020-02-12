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

package io.rsocket.routing.frames;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.Tags;

import static io.rsocket.routing.frames.FlyweightUtils.decodeByteStringLength;
import static io.rsocket.routing.frames.FlyweightUtils.decodeId;
import static io.rsocket.routing.frames.FlyweightUtils.encodeByteString;
import static io.rsocket.routing.frames.FlyweightUtils.encodeId;

/**
 * https://github.com/rsocket/rsocket/blob/feature/rf/Extensions/Routing-And-Forwarding.md#route_join
 */
public class RouteJoinFlyweight {

	public static ByteBuf encode(ByteBufAllocator allocator, Id brokerId, Id routeId, long timestamp, String serviceName, Tags tags) {
		Objects.requireNonNull(brokerId, "brokerId may not be null");
		Objects.requireNonNull(routeId, "routeId may not be null");
		Objects.requireNonNull(serviceName, "serviceName may not be null");
		Objects.requireNonNull(tags, "tags may not be null");

		ByteBuf byteBuf = FrameHeaderFlyweight.encode(allocator, FrameType.ROUTE_JOIN);
		encodeId(byteBuf, brokerId);
		encodeId(byteBuf, routeId);
		byteBuf.writeLong(timestamp);

		encodeByteString(byteBuf, serviceName);

		TagsFlyweight.encode(byteBuf, tags);

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

	public static String serviceName(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES + FlyweightUtils.ID_BYTES + Long.BYTES;
		return FlyweightUtils.decodeByteString(byteBuf, offset);
	}

	public static Tags tags(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES + FlyweightUtils.ID_BYTES + Long.BYTES;
		// serviceName length
		offset += decodeByteStringLength(byteBuf, offset);
		return TagsFlyweight.decode(offset, byteBuf);
	}

}
