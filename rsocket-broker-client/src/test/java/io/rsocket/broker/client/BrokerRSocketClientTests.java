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

package io.rsocket.broker.client;

import io.netty.buffer.CompositeByteBuf;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.CompositeMetadata.Entry;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.MimeTypes;
import io.rsocket.broker.frames.Address;
import io.rsocket.broker.frames.AddressFlyweight;
import io.rsocket.broker.frames.RoutingType;
import io.rsocket.transport.local.LocalClientTransport;
import org.junit.jupiter.api.Test;

import static io.rsocket.broker.common.WellKnownKey.SERVICE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class BrokerRSocketClientTests {

	public static final Id ORIGIN_ID = Id.from("00000000-0000-0000-0000-000000000001");

	@Test
	public void testAddressWithServiceNameOnly() {
		Route route = setupRoute();

		CompositeByteBuf composite = route.allocator().compositeBuffer();
		route.encodeAddressMetadata(composite, "remoteservice");

		Address address = assertAddress(composite);
		assertThat(address.getTags().asMap()).hasSize(1);
	}

	@Test
	public void testAddressWithMultipleTags() {
		Route route = setupRoute();

		CompositeByteBuf composite = route.allocator().compositeBuffer();
		route.encodeAddressMetadata(composite, tags -> tags.with(SERVICE_NAME, "remoteservice")
				.with("mykey", "mykeyvalue"));

		Address address = assertAddress(composite);
		assertThat(address.getTags().asMap()).hasSize(2);
		assertThat(address.getTags().get("mykey")).isEqualTo("mykeyvalue");
	}

	private Address assertAddress(CompositeByteBuf composite) {
		CompositeMetadata compositeMetadata = new CompositeMetadata(composite, true);
		Entry entry = compositeMetadata.stream().findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unable to decode"));
		assertThat(entry.getMimeType()).isEqualTo(MimeTypes.BROKER_FRAME_MIME_TYPE);
		Address address = Address.from(entry.getContent(), AddressFlyweight.FLAGS_M);
		assertThat(address).isNotNull();
		assertThat(address.getOriginRouteId()).isEqualTo(ORIGIN_ID);
		assertThat(address.getTags().get(SERVICE_NAME)).isEqualTo("remoteservice");
		assertThat(address.getRoutingType()).isEqualTo(RoutingType.MULTICAST);
		return address;
	}

	private Route setupRoute() {
		Route route = BrokerRSocketConnector.create()
				.routeId(ORIGIN_ID)
				.serviceName("localservice")
				.toRSocketClient(LocalClientTransport.create("local"));
		return route;
	}
}
