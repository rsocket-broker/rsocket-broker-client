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
import io.rsocket.broker.common.Tags;

import static io.rsocket.broker.frames.RouteSetupFlyweight.routeId;
import static io.rsocket.broker.frames.RouteSetupFlyweight.serviceName;
import static io.rsocket.broker.frames.RouteSetupFlyweight.tags;

/**
 * Representation of decoded RouteSetup information.
 */
public final class RouteSetup extends BrokerFrame {

	private final Id routeId;

	private final String serviceName;
	private final Tags tags;

	private RouteSetup(Id routeId, String serviceName, Tags tags) {
		super(FrameType.ROUTE_SETUP, 0);
		this.routeId = routeId;
		this.serviceName = serviceName;
		this.tags = tags;
	}

	public Id getRouteId() {
		return this.routeId;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public Tags getTags() {
		return this.tags;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", RouteSetup.class.getSimpleName() + "[", "]")
				.add("routeId=" + routeId)
				.add("serviceName='" + serviceName + "'")
				.add("tags=" + tags)
				.toString();
	}

	public static Builder from(Id id, String serviceName) {
		return new Builder(id, serviceName);
	}

	public static RouteSetup from(ByteBuf byteBuf) {
		return from(routeId(byteBuf), serviceName(byteBuf))
				.with(tags(byteBuf)).build();
	}

	public static final class Builder extends Tags.Builder<Builder> {

		private final Id id;

		private final String serviceName;

		private Builder(Id id, String serviceName) {
			Objects.requireNonNull(id, "id may not be null");
			Objects.requireNonNull(serviceName, "serviceName may not be null");
			if (serviceName.isEmpty()) {
				throw new IllegalArgumentException("serviceName may not be empty");
			}
			this.id = id;
			this.serviceName = serviceName;
		}

		public RouteSetup build() {
			return new RouteSetup(id, serviceName, buildTags());
		}

	}
}
