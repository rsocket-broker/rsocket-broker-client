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
import java.util.StringJoiner;

import io.netty.buffer.ByteBuf;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.Key;
import io.rsocket.broker.common.Tags;
import io.rsocket.broker.common.WellKnownKey;

import static io.rsocket.broker.frames.AddressFlyweight.FLAGS_E;
import static io.rsocket.broker.frames.AddressFlyweight.metadata;
import static io.rsocket.broker.frames.AddressFlyweight.originRouteId;
import static io.rsocket.broker.frames.AddressFlyweight.tags;

/**
 * Useful to hold decoded Address information.
 */
public class Address extends BrokerFrame {

	private final Id originRouteId;
	private final Tags metadata;
	private final Tags tags;

	private Address(Id originRouteId, Tags metadata, Tags tags, int flags) {
		super(FrameType.ADDRESS, flags);
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

	public boolean isEncrypted() {
		int flag = getFlags() & FLAGS_E;
		return flag == FLAGS_E;
	}

	public RoutingType getRoutingType() {
		int routingType = getFlags() & (~AddressFlyweight.ROUTING_TYPE_MASK);
		return RoutingType.from(routingType);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Address.class.getSimpleName() + "[", "]")
				.add("originRouteId=" + originRouteId)
				.add("metadata=" + metadata)
				.add("tags=" + tags)
				.add("flags=" + getFlags())
				.toString();
	}

	public static Builder from(Id originRouteId) {
		return new Builder(originRouteId);
	}

	public static Address from(ByteBuf byteBuf, int flags) {
		return from(originRouteId(byteBuf)).withMetadata(metadata(byteBuf))
				.with(tags(byteBuf)).flags(flags).build();
	}

	public static final class Builder extends Tags.Builder<Builder> {
		private final Id originRouteId;

		private final Tags.Builder<?> metadataBuilder = Tags.builder();

		private int flags = AddressFlyweight.FLAGS_U;

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

		public Builder encrypted() {
			flags |= AddressFlyweight.FLAGS_E;
			return this;
		}

		public Builder routingType(RoutingType routingType) {
			if (routingType == null) {
				throw new IllegalArgumentException("routingType may not be null");
			}
			// reset broker type flag
			flags &= AddressFlyweight.ROUTING_TYPE_MASK;
			flags |= routingType.getFlag();
			return this;
		}

		public Address build() {
			Tags tags = buildTags();
			if (tags.isEmpty()) {
				throw new IllegalArgumentException("Address tags may not be empty");
			}
			return new Address(originRouteId, metadataBuilder.buildTags(), tags, flags);
		}

		public Builder flags(int flags) {
			this.flags = flags;
			return this;
		}
	}
}
