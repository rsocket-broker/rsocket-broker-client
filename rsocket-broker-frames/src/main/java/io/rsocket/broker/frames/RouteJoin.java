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
import io.rsocket.broker.common.Tags;

/**
 * Representation of decoded RouteJoin information.
 */
public final class RouteJoin extends BrokerFrame {

	private final Id brokerId;

	private final Id routeId;

	private final long timestamp;

	private final String serviceName;

	private final Tags tags;

	public RouteJoin(Id brokerId, Id routeId, long timestamp,
			String serviceName, Tags tags) {
		super(FrameType.ROUTE_JOIN, 0);
		this.brokerId = brokerId;
		this.routeId = routeId;
		this.timestamp = timestamp;
		this.serviceName = serviceName;
		this.tags = tags;
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

	public String getServiceName() {
		return this.serviceName;
	}

	public Tags getTags() {
		return this.tags;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RouteJoin routeJoin = (RouteJoin) o;
		return this.timestamp == routeJoin.timestamp
				&& Objects.equals(this.brokerId, routeJoin.brokerId)
				&& Objects.equals(this.routeId, routeJoin.routeId)
				&& Objects.equals(this.serviceName, routeJoin.serviceName)
				&& Objects.equals(this.tags, routeJoin.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.brokerId, this.routeId, this.timestamp, this.serviceName,
				this.tags);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", RouteJoin.class.getSimpleName() + "[", "]")
				.add("brokerId=" + brokerId)
				.add("routeId=" + routeId)
				.add("timestamp=" + timestamp)
				.add("serviceName='" + serviceName + "'")
				.add("tags=" + tags)
				.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static RouteJoin from(ByteBuf byteBuf) {
		return builder()
				.brokerId(RouteJoinFlyweight.brokerId(byteBuf))
				.routeId(RouteJoinFlyweight.routeId(byteBuf))
				.timestamp(RouteJoinFlyweight.timestamp(byteBuf))
				.serviceName(RouteJoinFlyweight.serviceName(byteBuf))
				.with(RouteJoinFlyweight.tags(byteBuf))
				.build();
	}

	public static final class Builder extends Tags.Builder<Builder> {

		private Id brokerId;

		private Id routeId;

		private long timestamp = System.currentTimeMillis();

		private String serviceName;

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

		public Builder serviceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		public RouteJoin build() {
			Objects.requireNonNull(brokerId, "brokerId may not be null");
			Objects.requireNonNull(routeId, "routeId may not be null");
			Objects.requireNonNull(serviceName, "serviceName may not be null");
			if (timestamp <= 0) {
				throw new IllegalArgumentException("timestamp must be > 0");
			}
			return new RouteJoin(brokerId, routeId, timestamp, serviceName,
					buildTags());
		}

	}

}
