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
	/** (E)ncrypted flag: a value of 1 indicates the frame is encrypted */
	public static final int FLAGS_E = 0b01_0000_0000;

	private static final int FRAME_FLAGS_MASK = 0b0000_0011_1111_1111;
	private static final int FRAME_TYPE_BITS = 6;
	private static final int FRAME_TYPE_SHIFT = 16 - FRAME_TYPE_BITS;
	private static final int HEADER_SIZE = Short.BYTES;

	private FrameHeaderFlyweight() {}

	public static ByteBuf encode(
			final ByteBufAllocator allocator, final FrameType frameType, int flags) {
		if (!frameType.isEncryptable() && ((flags & FLAGS_E) == FLAGS_E)) {
			throw new IllegalStateException("bad value for encrypted flag");
		}

		short typeAndFlags = (short) (frameType.getEncodedType() << FRAME_TYPE_SHIFT | (short) flags);

		return allocator.buffer().writeShort(typeAndFlags);
	}

	public static int flags(final ByteBuf byteBuf) {
		short typeAndFlags = byteBuf.getShort(0);
		return typeAndFlags & FRAME_FLAGS_MASK;
	}

	public static boolean isEncrypted(ByteBuf byteBuf) {
		return (flags(byteBuf) & FLAGS_E) == FLAGS_E;
	}

	public static FrameType frameType(ByteBuf byteBuf) {
		int typeAndFlags = byteBuf.getShort(0);
		return FrameType.fromEncodedType(typeAndFlags >> FRAME_TYPE_SHIFT);
	}

	public static int size() {
		return HEADER_SIZE;
	}
}
