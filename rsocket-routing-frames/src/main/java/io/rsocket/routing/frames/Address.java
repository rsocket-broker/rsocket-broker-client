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
import java.util.StringJoiner;

import io.netty.buffer.ByteBuf;
import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.Key;
import io.rsocket.routing.common.Tags;
import io.rsocket.routing.common.WellKnownKey;

import static io.rsocket.routing.frames.AddressFlyweight.metadata;
import static io.rsocket.routing.frames.AddressFlyweight.originRouteId;
import static io.rsocket.routing.frames.AddressFlyweight.tags;

/**
 * Useful to hold decoded Address information.
 */
public class Address extends RoutingFrame {

	private final Id originRouteId;
	private final Tags metadata;
	private final Tags tags;

	private Address(Id originRouteId, Tags metadata, Tags tags) {
		super(FrameType.ADDRESS);
		this.originRouteId = originRouteId;
		this.metadata = metadata;
		this.tags = tags;
	}

	public Id getOriginRouteId() {
		return this.originRouteId;
	}

	public Tags getMetadata() {
		return this.metadata;
	}

	public Tags getTags() {
		return this.tags;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Address.class.getSimpleName() + "[", "]")
				.add("originRouteId=" + originRouteId)
				.add("metadata=" + metadata)
				.add("tags=" + tags)
				.toString();
	}

	public static Builder from(Id originRouteId) {
		return new Builder(originRouteId);
	}

	public static Address from(ByteBuf byteBuf) {
		return from(originRouteId(byteBuf)).withMetadata(metadata(byteBuf))
				.with(tags(byteBuf)).build();
	}

	public static final class Builder extends Tags.Builder<Builder> {
		private final Id originRouteId;

		private final Tags.Builder<?> metadataBuilder = Tags.builder();

		private Builder(Id originRouteId) {
			Objects.requireNonNull(originRouteId, "id may not be null");
			this.originRouteId = originRouteId;
		}

		public Builder withMetadata(String key, String value) {
			metadataBuilder.with(key, value);
			return this;
		}

		public Builder withMetadata(WellKnownKey key, String value) {
			metadataBuilder.with(key, value);
			return this;
		}

		public Builder withMetadata(Key key, String value) {
			metadataBuilder.with(key, value);
			return this;
		}

		public Builder withMetadata(Tags metadata) {
			metadataBuilder.with(metadata);
			return this;
		}

		public Address build() {
			Tags tags = buildTags();
			if (tags.isEmpty()) {
				throw new IllegalArgumentException("Address tags may not be empty");
			}
			return new Address(originRouteId, metadataBuilder.buildTags(), tags);
		}
	}
}
