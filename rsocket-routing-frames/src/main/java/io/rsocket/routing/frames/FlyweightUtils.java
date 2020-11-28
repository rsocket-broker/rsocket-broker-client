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

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.rsocket.routing.common.Id;

public class FlyweightUtils {

	public static final int ID_BYTES = 16;
	private static final int UNSIGNED_BYTE_SIZE = 8;
	private static final int UNSIGNED_BYTE_MAX_VALUE = (1 << UNSIGNED_BYTE_SIZE) - 1;

	static void encodeByteString(ByteBuf byteBuf, String s) {
		int length = requireUnsignedByte(ByteBufUtil.utf8Bytes(s));
		byteBuf.writeByte(length);
		ByteBufUtil.reserveAndWriteUtf8(byteBuf, s, length);
	}

	static String decodeByteString(ByteBuf byteBuf, int offset) {
		int length = byteBuf.getByte(offset);
		length &= UNSIGNED_BYTE_MAX_VALUE;
		offset += Byte.BYTES;

		return byteBuf.toString(offset, length, StandardCharsets.UTF_8);
	}

	static int decodeByteStringLength(ByteBuf byteBuf, int offset) {
		int length = byteBuf.getByte(offset);
		length &= UNSIGNED_BYTE_MAX_VALUE;
		return Byte.BYTES + length;
	}

	/**
	 * Requires that an {@code int} can be represented as an unsigned {@code byte}.
	 *
	 * @param i the {@code int} to test
	 * @return the {@code int} if it can be represented as an unsigned {@code byte}
	 * @throws IllegalArgumentException if {@code i} cannot be represented as an unsigned {@code byte}
	 */
	static int requireUnsignedByte(int i) {
		if (i > UNSIGNED_BYTE_MAX_VALUE) {
			throw new IllegalArgumentException(
					String.format("%d is larger than %d bits", i, UNSIGNED_BYTE_SIZE));
		}

		return i;
	}

	static void encodeId(ByteBuf byteBuf, Id id) {
		byteBuf.writeLong(id.getFirst());
		byteBuf.writeLong(id.getSecond());
	}

	static Id decodeId(ByteBuf byteBuf, int offset) {
		long first = byteBuf.getLong(offset);
		long second = byteBuf.getLong(offset + Long.BYTES);
		return new Id(first, second);
	}
}
