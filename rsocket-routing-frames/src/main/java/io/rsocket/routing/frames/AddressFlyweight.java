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

import static io.rsocket.routing.frames.FlyweightUtils.decodeId;
import static io.rsocket.routing.frames.FlyweightUtils.encodeId;

/**
 * https://github.com/rsocket/rsocket/blob/feature/rf/Extensions/Routing-And-Forwarding.md#address
 */
public class AddressFlyweight {

	public static ByteBuf encode(ByteBufAllocator allocator, Id originRouteId, Tags metadata, Tags tags) {
		Objects.requireNonNull(originRouteId, "originRouteId may not be null");
		Objects.requireNonNull(tags, "tags may not be null");

		ByteBuf byteBuf = FrameHeaderFlyweight.encode(allocator, FrameType.ROUTE_SETUP);
		encodeId(byteBuf, originRouteId);

		//FIXME: metadata missing
		TagsFlyweight.encode(byteBuf, metadata);

		TagsFlyweight.encode(byteBuf, tags);

		return byteBuf;
	}

	public static Id originRouteId(ByteBuf byteBuf) {
		return decodeId(byteBuf, FrameHeaderFlyweight.BYTES);
	}

	public static Tags metadata(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		return TagsFlyweight.decode(offset, byteBuf);
	}

	public static Tags tags(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		// metadata length
		offset += TagsFlyweight.length(offset, byteBuf);
		return TagsFlyweight.decode(offset, byteBuf);
	}

}
