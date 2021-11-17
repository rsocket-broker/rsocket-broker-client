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

package io.rsocket.broker.config;

import io.rsocket.broker.common.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArchaiusSampleTests {

	private ArchaiusBrokerClientProperties properties;

	@BeforeEach
	public void setup() {
		/*System.setProperty(BrokerClientProperties.CONFIG_PREFIX + ".serviceName",
				"servicefromproperties");*/
		properties = ArchaiusConfigFactory.load("archaiustest1");
	}

	@Test
	// https://github.com/Netflix/archaius/blob/2.x/archaius2-core/src/test/java/com/netflix/archaius/mapper/ProxyFactoryTest.java
	public void configWorks() {
		assertThat(properties.getServiceName()).isEqualTo("testservice");
		assertThat(properties.getRouteId()).isEqualTo(Id.from("00000000-0000-0000-0000-000000000022"));
	}
}
