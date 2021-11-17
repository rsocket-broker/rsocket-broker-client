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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.Tags;
import io.rsocket.broker.common.WellKnownKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouteSetupFlyweightTests {

	@Test
	void testEncodeDecode() {
		Id routeId = Id.random();
		assertEncodeDecode(routeId);
	}

	@Test
	void testEncodeDecodeMaxId() {
		Id routeId = new Id(Long.MAX_VALUE, Long.MAX_VALUE);
		assertEncodeDecode(routeId);
	}

	@Test
	void testEncodeDecodeMinId() {
		Id routeId = new Id(0, 0);
		assertEncodeDecode(routeId);
	}

	private void assertEncodeDecode(Id routeId) {
		String serviceName = "myService";
		Tags tags = Tags.builder().with(WellKnownKey.MAJOR_VERSION, "1")
				.with(WellKnownKey.MINOR_VERSION, "0")
				.with("mycustomtag", "mycustomtagvalue")
				.buildTags();
		ByteBuf encoded = RouteSetupFlyweight
				.encode(ByteBufAllocator.DEFAULT, routeId, serviceName, tags, 0);
		assertThat(FrameHeaderFlyweight.frameType(encoded)).isEqualTo(FrameType.ROUTE_SETUP);
		assertThat(RouteSetupFlyweight.routeId(encoded)).isEqualTo(routeId);
		assertThat(RouteSetupFlyweight.serviceName(encoded)).isEqualTo(serviceName);
		assertThat(RouteSetupFlyweight.tags(encoded)).isEqualTo(tags);
	}

}
