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

import java.math.BigInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouteRemoveFlyweightTests {

	@Test
	void testEncodeDecode() {
		BigInteger brokerId = BigInteger.valueOf(456L);
		BigInteger routeId = BigInteger.valueOf(123L);
		long timestamp = System.currentTimeMillis();

		ByteBuf encoded = RouteRemoveFlyweight
				.encode(ByteBufAllocator.DEFAULT, brokerId, routeId, timestamp);
		assertThat(RouteRemoveFlyweight.brokerId(encoded)).isEqualTo(brokerId);
		assertThat(RouteRemoveFlyweight.routeId(encoded)).isEqualTo(routeId);
		assertThat(RouteRemoveFlyweight.timestamp(encoded)).isEqualTo(timestamp);
	}

}
