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

import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.DefaultConfigLoader;
import com.netflix.archaius.DefaultPropertyFactory;
import com.netflix.archaius.Layers;
import com.netflix.archaius.api.config.CompositeConfig;
import com.netflix.archaius.api.config.LayeredConfig;
import com.netflix.archaius.api.exceptions.ConfigException;
import com.netflix.archaius.config.DefaultCompositeConfig;
import com.netflix.archaius.config.DefaultLayeredConfig;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.SystemConfig;
import reactor.core.Exceptions;

public class ArchaiusConfigFactory {

	public static RoutingClientProperties load() {
		return load("broker");
	}

	public static RoutingClientProperties load(String configname) {
		try {
			LayeredConfig layeredConfig = new DefaultLayeredConfig("layered-routing-broker");
			layeredConfig.addConfig(Layers.ENVIRONMENT, EnvironmentConfig.INSTANCE);
			layeredConfig.addConfig(Layers.SYSTEM, SystemConfig.INSTANCE);


			DefaultConfigLoader loader = DefaultConfigLoader.builder()
					//.withStrLookup(config)
					//.withDefaultCascadingStrategy(ConcatCascadeStrategy.from("${env}"))
					.build();

			CompositeConfig application = new DefaultCompositeConfig();
			application.replaceConfig(configname, loader.newLoader().load(configname));

			layeredConfig.addConfig(Layers.APPLICATION, application);

			DefaultSettableConfig defaults = new DefaultSettableConfig();
			// TODO: set defaults
			layeredConfig.addConfig(Layers.DEFAULT, defaults);

			DefaultPropertyFactory propertyFactory = new DefaultPropertyFactory(layeredConfig);
			ConfigProxyFactory factory = new ConfigProxyFactory(layeredConfig,
					layeredConfig.getDecoder(), propertyFactory);
			return factory.newProxy(RoutingClientProperties.class, RoutingClientProperties.CONFIG_PREFIX);
		}
		catch (ConfigException e) {
			throw Exceptions.bubble(e);
		}
	}

}
