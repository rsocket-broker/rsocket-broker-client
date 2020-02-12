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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * https://github.com/rsocket/rsocket/blob/feature/rf/Extensions/Routing-And-Forwarding.md#framing-header-format
 */
public class FrameHeaderFlyweight {

	public static final short MAJOR_VERSION = 0;
	public static final short MINOR_VERSION = 1;

	private static final int MAJOR_VERSION_SIZE = Short.BYTES;
	private static final int MINOR_VERSION_SIZE = Short.BYTES;
	private static final int FRAME_TYPE_SIZE = Short.BYTES;
	private static final int RESERVED_BITS = 10;

	public static final int BYTES = MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE + FRAME_TYPE_SIZE;

	public static ByteBuf encode(ByteBufAllocator allocator, FrameType frameType) {
		return encode(allocator, MAJOR_VERSION, MINOR_VERSION, frameType);
	}

	public static ByteBuf encode(ByteBufAllocator allocator, short majorVersion,
			short minorVersion, FrameType frameType) {
		int id = frameType.getId();
		// move id left 10 bits
		id = id << RESERVED_BITS;
		return allocator.buffer()
				.writeShort(majorVersion)
				.writeShort(minorVersion)
				//TODO: first 6 bits only?
				.writeShort(id);
	}

	public static short majorVersion(ByteBuf byteBuf) {
		return byteBuf.getShort(0);
	}

	public static short minorVersion(ByteBuf byteBuf) {
		return byteBuf.getShort(MAJOR_VERSION_SIZE);
	}

	public static FrameType frameType(ByteBuf byteBuf) {
		int id = byteBuf.getShort(MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE);
		// move id right 10 bits
		id = id >> RESERVED_BITS;
		return FrameType.from(id);
	}
}
