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
 * https://github.com/rsocket-broker/rsocket-broker-spec/blob/master/RSocketBrokerSpecification.md#broker_info
 */
public class BrokerInfoFlyweight {

	public static ByteBuf encode(ByteBufAllocator allocator, Id brokerId, long timestamp, Tags tags, int flags) {
		Objects.requireNonNull(brokerId, "brokerId may not be null");
		Objects.requireNonNull(tags, "tags may not be null");

		ByteBuf byteBuf = FrameHeaderFlyweight.encode(allocator, FrameType.BROKER_INFO, flags);
		encodeId(byteBuf, brokerId);

		byteBuf.writeLong(timestamp);

		TagsFlyweight.encode(byteBuf, tags);

		return byteBuf;
	}

	public static Id brokerId(ByteBuf byteBuf) {
		return decodeId(byteBuf, FrameHeaderFlyweight.BYTES);
	}

	public static long timestamp(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES;
		return byteBuf.getLong(offset);
	}

	public static Tags tags(ByteBuf byteBuf) {
		int offset = FrameHeaderFlyweight.BYTES + FlyweightUtils.ID_BYTES + Long.BYTES;
		return TagsFlyweight.decode(offset, byteBuf);
	}

}
