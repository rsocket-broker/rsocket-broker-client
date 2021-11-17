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

/**
 * https://github.com/rsocket-broker/rsocket-broker-spec/blob/master/RSocketBrokerSpecification.md#frame-types
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
	ROUTE_JOIN(0x02),

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
	ADDRESS(0x05);

	private static FrameType[] frameTypesById;

	static {
		frameTypesById = new FrameType[values().length];

		for (FrameType frameType : values()) {
			frameTypesById[frameType.id] = frameType;
		}
	}

	private final int id;

	FrameType(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public static FrameType from(int id) {
		return frameTypesById[id];
	}
}
