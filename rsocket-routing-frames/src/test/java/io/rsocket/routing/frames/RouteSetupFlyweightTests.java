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

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.routing.common.Tags;
import io.rsocket.routing.common.WellKnownKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouteSetupFlyweightTests {

	@Test
	void testEncodeDecode() {
		UUID routeId = UUID.randomUUID();
		String serviceName = "myService";
		Tags tags = Tags.of(WellKnownKey.MAJOR_VERSION, "1")
				.and(WellKnownKey.MINOR_VERSION, "0")
				.and("mycustomtag", "mycustomtagvalue");
		ByteBuf encoded = RouteSetupFlyweight
				.encode(ByteBufAllocator.DEFAULT, routeId, serviceName, tags);
		assertThat(RouteSetupFlyweight.routeId(encoded)).isEqualTo(routeId);
		assertThat(RouteSetupFlyweight.serviceName(encoded)).isEqualTo(serviceName);
		assertThat(RouteSetupFlyweight.tags(encoded)).isEqualTo(tags);
	}

}
