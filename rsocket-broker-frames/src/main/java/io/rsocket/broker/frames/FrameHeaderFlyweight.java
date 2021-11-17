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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * https://github.com/rsocket-broker/rsocket-broker-spec/blob/master/RSocketBrokerSpecification.md#framing-header-format
 */
public class FrameHeaderFlyweight {

	public static final short MAJOR_VERSION = 0;
	public static final short MINOR_VERSION = 1;

	private static final int MAJOR_VERSION_SIZE = Short.BYTES;
	private static final int MINOR_VERSION_SIZE = Short.BYTES;
	private static final int FRAME_TYPE_SIZE = Short.BYTES;
	private static final int FRAME_FLAGS_MASK = 0b0000_0011_1111_1111;
	private static final int FLAG_BITS = 10;

	public static final int BYTES = MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE + FRAME_TYPE_SIZE;

	public static ByteBuf encode(ByteBufAllocator allocator, FrameType frameType, int flags) {
		return encode(allocator, MAJOR_VERSION, MINOR_VERSION, frameType, flags);
	}

	public static ByteBuf encode(ByteBufAllocator allocator, short majorVersion,
			short minorVersion, FrameType frameType, int flags) {
		//TODO: check that only one broker flag is set
		//if (!frameType.canHaveMetadata() && ((flags & FLAGS_M) == FLAGS_M)) {
		//	throw new IllegalStateException("bad value for metadata flag");
		//}
		int frameId = frameType.getId() << FLAG_BITS;
		short typeAndFlags = (short) (frameId | (short) flags);
		return allocator.buffer()
				.writeShort(majorVersion)
				.writeShort(minorVersion)
				.writeShort(typeAndFlags);
	}

	public static short majorVersion(ByteBuf byteBuf) {
		return byteBuf.getShort(0);
	}

	public static short minorVersion(ByteBuf byteBuf) {
		return byteBuf.getShort(MAJOR_VERSION_SIZE);
	}

	public static int flags(final ByteBuf byteBuf) {
		if (!byteBuf.isReadable(MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE + Short.BYTES)) {
			return 0;
		}
		byteBuf.markReaderIndex();
		byteBuf.skipBytes(MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE);
		short typeAndFlags = byteBuf.readShort();
		byteBuf.resetReaderIndex();
		return typeAndFlags & FRAME_FLAGS_MASK;
	}

	public static FrameType frameType(ByteBuf byteBuf) {
		if (!byteBuf.isReadable(MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE + Short.BYTES)) {
			return null;
		}
		byteBuf.markReaderIndex();
		byteBuf.skipBytes(MAJOR_VERSION_SIZE + MINOR_VERSION_SIZE);
		short typeAndFlags = byteBuf.readShort();
		byteBuf.resetReaderIndex();
		// move typeAndFlags right 10 bits
		return FrameType.from(typeAndFlags >> FLAG_BITS);
	}
}
