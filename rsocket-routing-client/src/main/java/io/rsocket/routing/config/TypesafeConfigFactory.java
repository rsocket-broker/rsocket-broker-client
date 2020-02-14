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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;

public class TypesafeConfigFactory {

	public static RoutingClientProperties load() {
		return load(ConfigFactory.load());
	}

	public static RoutingClientProperties load(Config config) {
		RoutingClientProperties configuration = ConfigBeanFactory.create(config
				.getConfig(RoutingClientProperties.CONFIG_PREFIX), RoutingClientProperties.class);
		return configuration;
	}
}
