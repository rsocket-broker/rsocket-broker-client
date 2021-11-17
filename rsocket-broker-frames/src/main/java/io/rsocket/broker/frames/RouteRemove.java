/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

/**
 * Representation of decoded RouteSetup information.
 */
public final class RouteRemove extends BrokerFrame {

	private final Id brokerId;

	private final Id routeId;

	private final long timestamp;

	public RouteRemove(Id brokerId, Id routeId, long timestamp) {
		super(FrameType.ROUTE_REMOVE, 0);
		this.brokerId = brokerId;
		this.routeId = routeId;
		this.timestamp = timestamp;
	}

	public Id getBrokerId() {
		return this.brokerId;
	}

	public Id getRouteId() {
		return this.routeId;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RouteRemove routeJoin = (RouteRemove) o;
		return this.timestamp == routeJoin.timestamp
				&& Objects.equals(this.brokerId, routeJoin.brokerId)
				&& Objects.equals(this.routeId, routeJoin.routeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.brokerId, this.routeId, this.timestamp);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", RouteRemove.class.getSimpleName() + "[", "]")
				.add("brokerId=" + brokerId)
				.add("routeId=" + routeId)
				.add("timestamp=" + timestamp)
				.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static RouteRemove from(ByteBuf byteBuf) {
		return builder()
				.brokerId(RouteRemoveFlyweight.brokerId(byteBuf))
				.routeId(RouteRemoveFlyweight.routeId(byteBuf))
				.timestamp(RouteRemoveFlyweight.timestamp(byteBuf))
				.build();
	}

	public static final class Builder {

		private Id brokerId;

		private Id routeId;

		private long timestamp = System.currentTimeMillis();

		public Builder brokerId(Id brokerId) {
			this.brokerId = brokerId;
			return this;
		}

		public Builder routeId(Id routeId) {
			this.routeId = routeId;
			return this;
		}

		public Builder timestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public RouteRemove build() {
			Objects.requireNonNull(brokerId, "brokerId may not be null");
			Objects.requireNonNull(routeId, "brokerId may not be null");
			if (timestamp <= 0) {
				throw new IllegalArgumentException("timestamp must be > 0");
			}
			return new RouteRemove(brokerId, routeId, timestamp);
		}

	}

}
