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

package io.rsocket.routing.config;

import com.typesafe.config.ConfigFactory;
import io.rsocket.routing.common.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TypesafeConfigFactoryTests {

	private RoutingClientProperties config;

	@BeforeEach
	public void setup() {
		config = TypesafeConfigFactory.load(ConfigFactory.load("typesafetest1"));
	}

	@Test
	@Disabled
	// TODO: typesafe doesn't work for arbitrary objects, maybe transition to
	// https://github.com/Netflix/archaius/blob/2.x/archaius2-core/src/test/java/com/netflix/archaius/mapper/ProxyFactoryTest.java
	public void configWorks() {
		assertThat(config.getRouteId()).isEqualTo(Id.from("00000000-0000-0000-0000-000000000001"));
	}
}
