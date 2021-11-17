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

package io.rsocket.broker.client.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.Key;
import io.rsocket.broker.common.Tags;
import io.rsocket.broker.frames.Address;
import org.junit.jupiter.api.Test;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.RequestSpec;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.RouteMatcher;
import org.springframework.util.SimpleRouteMatcher;

import static io.rsocket.broker.client.spring.BrokerRSocketRequester.address;
import static io.rsocket.broker.client.spring.BrokerRSocketRequester.expand;
import static io.rsocket.broker.common.WellKnownKey.ROUTE_ID;
import static io.rsocket.broker.common.WellKnownKey.SERVICE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BrokerRSocketRequesterTests {

	@Test
	public void addressWorks() {
		RouteMatcher routeMatcher = new SimpleRouteMatcher(new AntPathMatcher("."));
		RouteMatcher.Route route = routeMatcher.parseRoute("myroute.foo1.bar1");
		Tags tags = Tags.builder()
				.with(SERVICE_NAME, "{foo}")
				.with(ROUTE_ID, "22")
				.with("mycustomkey", "{foo}-{bar}")
				.buildTags();

		Address addr = address(routeMatcher, route,
				Id.from("00000000-0000-0000-0000-000000000011"), "myroute.{foo}.{bar}", tags.asMap()).build();

		assertThat(addr).isNotNull();
		assertThat(addr.getTags().asMap()).isNotEmpty()
				.containsEntry(Key.of(SERVICE_NAME), "foo1")
				.containsEntry(Key.of(ROUTE_ID), "22")
				.containsEntry(Key.of("mycustomkey"), "foo1-bar1");
	}

	@Test
	public void expandArrayVars() {
		String result = expand("myroute.{foo}.{bar}", "foo1", "bar1");
		assertThat(result).isEqualTo("myroute.foo1.bar1");
	}

	@Test
	public void expandMapVars() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("value", "a+b");
		map.put("city", "Z\u00fcrich");
		String result = expand("/hotel list/{city} specials/{value}", map);

		assertThat(result).isEqualTo("/hotel list/Z\u00fcrich specials/a+b");
	}

	@Test
	public void expandPartially() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("city", "Z\u00fcrich");
		String result = expand("/hotel list/{city} specials/{value}", map);

		assertThat(result).isEqualTo("/hotel list/Zürich specials/");
	}

	@Test
	public void expandSimple() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("foo", "1 2");
		map.put("bar", "3 4");
		String result = expand("/{foo} {bar}", map);
		assertThat(result).isEqualTo("/1 2 3 4");
	}

	@Test // SPR-13311
	public void expandWithRegexVar() {
		String template = "/myurl/{name:[a-z]{1,5}}/show";
		Map<String, String> map = Collections.singletonMap("name", "test");
		String result = expand(template, map);
		assertThat(result).isEqualTo("/myurl/test/show");
	}

	@Test // SPR-17630
	public void expandWithMismatchedCurlyBraces() {
		String result = expand("/myurl/{{{{", Collections.emptyMap());
		assertThat(result).isEqualTo("/myurl/{{{{");
	}

	// if BrokerRequestSpec.address() calls delegate.metadata() this fails with an exception
	@Test
	public void routeWithAddress() {
		RequestSpec spec = mock(RequestSpec.class);
		RSocketRequester requester = mock(RSocketRequester.class);
		when(requester.route("route")).thenReturn(spec);
		BrokerClientProperties properties = new BrokerClientProperties();
		RouteMatcher routeMatcher = mock(RouteMatcher.class);

		BrokerRSocketRequester brokerRSocketRequester = new BrokerRSocketRequester(requester, properties, routeMatcher);
		brokerRSocketRequester
				.route("route")
				.address("service")
				.send();
	}

}
