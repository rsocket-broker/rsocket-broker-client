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

import java.util.HashMap;
import java.util.Map;

public enum RoutingType {
	UNICAST(AddressFlyweight.FLAGS_U),
	MULTICAST(AddressFlyweight.FLAGS_M),
	SHARD(AddressFlyweight.FLAGS_S);

	private static Map<Integer, RoutingType> routingTypesByFlag;

	static {
		routingTypesByFlag = new HashMap<>();

		for (RoutingType routingType : values()) {
			routingTypesByFlag.put(routingType.getFlag(), routingType);
		}
	}

	private int flag;

	RoutingType(int flag) {
		this.flag = flag;
	}

	public int getFlag() {
		return this.flag;
	}

	public static RoutingType from(int flag) {
		return routingTypesByFlag.get(flag);
	}
}
