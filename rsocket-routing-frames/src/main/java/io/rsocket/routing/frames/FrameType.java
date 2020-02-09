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

import java.util.Arrays;

/**
 * https://github.com/rsocket/rsocket/blob/feature/rf/Extensions/Routing-And-Forwarding.md#frame-types
 */
public enum FrameType {
	/**
	 * RESERVED
	 */
	RESERVED(0x00),

	/**
	 * Information a routable destination sends to a broker
	 */
	ROUTE_SETUP(0x01),

	/**
	 * Information passed between brokers when a routable destination connects. This
	 * information may not arrive from the broker where the routable destination
	 * connected, so the information could be forwarded when it connects.
	 */
	ROUTE_ADD(0x02),

	/**
	 * Information passed between brokers to indicate a routable destination is no
	 * longer available.
	 */
	ROUTE_REMOVE(0x03),

	/**
	 * Information a broker passes to another broker .
	 */
	BROKER_INFO(0x04),

	/**
	 * A frame that contain information forwarding a message from an origin to a
	 * destination. This frame is intended for the metadata field.
	 */
	UNICAST(0x05, Flags.IS_ENCRYPTABLE),

	/**
	 * A frame that contain information forwarding a message from an origin to a
	 * destination. This frame is intended for the metadata field.
	 */
	MULTICAST(0x06, Flags.IS_ENCRYPTABLE),

	/**
	 * A frame that contain information forwarding a message from an origin to a
	 * destination. This frame is intended for the metadata field.
	 */
	SHARD(0x05, Flags.IS_ENCRYPTABLE);

	private static final FrameType[] FRAME_TYPES_BY_ENCODED_TYPE;

	static {
		FRAME_TYPES_BY_ENCODED_TYPE = new FrameType[getMaximumEncodedType() + 1];

		for (FrameType frameType : values()) {
			FRAME_TYPES_BY_ENCODED_TYPE[frameType.encodedType] = frameType;
		}
	}

	private final int encodedType;
	private final int flags;

	FrameType(int encodedType) {
		this(encodedType, Flags.EMPTY);
	}

	FrameType(int encodedType, int flags) {
		this.encodedType = encodedType;
		this.flags = flags;
	}

	/**
	 * Returns the {@code FrameType} that matches the specified {@code encodedType}.
	 *
	 * @param encodedType the encoded type
	 * @return the {@code FrameType} that matches the specified {@code encodedType}
	 */
	public static FrameType fromEncodedType(int encodedType) {
		FrameType frameType = FRAME_TYPES_BY_ENCODED_TYPE[encodedType];

		if (frameType == null) {
			throw new IllegalArgumentException(String.format("Frame type %d is unknown", encodedType));
		}

		return frameType;
	}

	private static int getMaximumEncodedType() {
		return Arrays.stream(values()).mapToInt(frameType -> frameType.encodedType).max().orElse(0);
	}

	/**
	 * Returns the encoded type.
	 *
	 * @return the encoded type
	 */
	public int getEncodedType() {
		return encodedType;
	}

	/**
	 * Whether the frame type is encryptable.
	 *
	 * @return whether the frame type is encryptable
	 */
	public boolean isEncryptable() {
		return Flags.IS_ENCRYPTABLE == (flags & Flags.IS_ENCRYPTABLE);
	}

	private static class Flags {
		private static final int EMPTY = 0b00000;
		private static final int IS_ENCRYPTABLE = 0b00001;

		private Flags() {}
	}
}
