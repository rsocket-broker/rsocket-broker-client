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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.rsocket.broker.common.Key;
import io.rsocket.broker.common.Tags;
import io.rsocket.broker.common.WellKnownKey;

public class TagsFlyweight {
	private static final int WELL_KNOWN_TAG = 0x80;
	private static final int HAS_MORE_TAGS = 0x80;
	private static final int MAX_TAG_LENGTH = 0x7F;

	public static ByteBuf encode(ByteBuf byteBuf, Tags tags) {
		Objects.requireNonNull(byteBuf, "byteBuf may not be null");

		Iterator<Map.Entry<Key, String>> it = tags.asMap().entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Key, String> entry = it.next();
			Key key = entry.getKey();
			if (key.getWellKnownKey() != null) {
				byte id = key.getWellKnownKey().getIdentifier();
				int keyLength = WELL_KNOWN_TAG | id;
				byteBuf.writeByte(keyLength);
			}
			else {
				String keyString = key.getKey();
				if (keyString == null) {
					continue;
				}
				int keyLength = ByteBufUtil.utf8Bytes(keyString);
				if (keyLength == 0 || keyLength > MAX_TAG_LENGTH) {
					continue;
				}
				byteBuf.writeByte(keyLength);
				ByteBufUtil.reserveAndWriteUtf8(byteBuf, keyString, keyLength);
			}

			boolean hasMoreTags = it.hasNext();

			String value = entry.getValue();
			int valueLength = ByteBufUtil.utf8Bytes(value);
			if (valueLength == 0 || valueLength > MAX_TAG_LENGTH) {
				continue;
			}
			int valueByte;
			if (hasMoreTags) {
				valueByte = HAS_MORE_TAGS | valueLength;
			}
			else {
				valueByte = valueLength;
			}
			byteBuf.writeByte(valueByte);
			ByteBufUtil.reserveAndWriteUtf8(byteBuf, value, valueLength);
		}

		return byteBuf;
	}

	public static Tags decode(int offset, ByteBuf byteBuf) {

		Tags.Builder builder = Tags.builder();

		// this means we've reached the end of the buffer
		if (offset >= byteBuf.writerIndex()) {
			return builder.buildTags();
		}

		boolean hasMoreTags = true;

		while (hasMoreTags) {
			int keyByte = byteBuf.getByte(offset);
			offset += Byte.BYTES;

			boolean isWellKnownTag = (keyByte & WELL_KNOWN_TAG) == WELL_KNOWN_TAG;

			int keyLengthOrId = keyByte & MAX_TAG_LENGTH;

			Key key;
			if (isWellKnownTag) {
				WellKnownKey wellKnownKey = WellKnownKey.fromIdentifier(keyLengthOrId);
				key = Key.of(wellKnownKey);
			}
			else {
				String keyString = byteBuf.toString(offset, keyLengthOrId, StandardCharsets.UTF_8);
				offset += keyLengthOrId;
				key = Key.of(keyString);
			}

			int valueByte = byteBuf.getByte(offset);
			offset += Byte.BYTES;

			hasMoreTags = (valueByte & HAS_MORE_TAGS) == HAS_MORE_TAGS;
			int valueLength = valueByte & MAX_TAG_LENGTH;
			String value = byteBuf.toString(offset, valueLength,
					StandardCharsets.UTF_8);
			offset += valueLength;

			builder.with(key, value);
		}

		return builder.buildTags();
	}

	// same algorithm as decode except no object creation, length is final offset minus original.
	public static int length(int offset, ByteBuf byteBuf) {

		int originalOffset = offset;

		// this means we've reached the end of the buffer
		if (offset >= byteBuf.writerIndex()) {
			return 0;
		}

		boolean hasMoreTags = true;

		while (hasMoreTags) {
			int keyByte = byteBuf.getByte(offset);
			offset += Byte.BYTES;

			boolean isWellKnownTag = (keyByte & WELL_KNOWN_TAG) == WELL_KNOWN_TAG;

			if (!isWellKnownTag) {
				int keyLength = keyByte & MAX_TAG_LENGTH;
				offset += keyLength;
			}

			int valueByte = byteBuf.getByte(offset);
			offset += Byte.BYTES;

			hasMoreTags = (valueByte & HAS_MORE_TAGS) == HAS_MORE_TAGS;
			int valueLength = valueByte & MAX_TAG_LENGTH;
			offset += valueLength;
		}

		return offset - originalOffset;
	}
}
