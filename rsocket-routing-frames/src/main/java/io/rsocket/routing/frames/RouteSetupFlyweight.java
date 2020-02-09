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

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.routing.common.Tags;
import io.rsocket.frame.VersionFlyweight;

/**
 * https://github.com/rsocket/rsocket/blob/feature/rf/Extensions/Routing-And-Forwarding.md#route_setup
 */
public class RouteSetupFlyweight {
	public static final int CURRENT_VERSION = VersionFlyweight.encode(0, 1);

	public static ByteBuf encode(ByteBufAllocator allocator, UUID routeId, String serviceName, Tags tags) {
		Objects.requireNonNull(routeId, "routeId may not be null");
		Objects.requireNonNull(serviceName, "serviceName may not be null");
		Objects.requireNonNull(tags, "tags may not be null");

		ByteBuf byteBuf = FrameHeaderFlyweight.encode(allocator, FrameType.ROUTE_SETUP, 0);

		byteBuf.writeInt(CURRENT_VERSION)
				.writeLong(routeId.getMostSignificantBits())
				.writeLong(routeId.getLeastSignificantBits());

		FlyweightUtils.encodeByteString(byteBuf, serviceName);

		TagsFlyweight.encode(byteBuf, tags);

		return byteBuf;
	}

	public static BigInteger routeId(ByteBuf byteBuf) {
		return decodeId(byteBuf, FrameHeaderFlyweight.BYTES);
	}

	public static String serviceName(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		return FlyweightUtils.decodeByteString(byteBuf, offset);
	}

	public static Tags tags(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		// serviceName length
		offset += decodeByteStringLength(byteBuf, offset);
		return TagsFlyweight.decode(offset, byteBuf);
	}

}
