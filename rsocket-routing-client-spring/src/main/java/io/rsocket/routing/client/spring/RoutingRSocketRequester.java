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

package io.rsocket.routing.client.spring;

import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rsocket.RSocket;
import io.rsocket.RSocketClient;
import io.rsocket.routing.common.Id;
import io.rsocket.routing.common.Key;
import io.rsocket.routing.common.WellKnownKey;
import io.rsocket.routing.frames.Address;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.RouteMatcher;

public final class RoutingRSocketRequester implements RSocketRequester {

	/** For route variable replacement. */
	private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

	private final RSocketRequester delegate;

	private final RoutingClientProperties properties;

	private final RouteMatcher routeMatcher;

	RoutingRSocketRequester(RSocketRequester delegate, RoutingClientProperties properties,
			RouteMatcher routeMatcher) {
		this.delegate = delegate;
		this.properties = properties;
		this.routeMatcher = routeMatcher;
	}

	@Override
	public RSocket rsocket() {
		return delegate.rsocket();
	}

	@Override
	public RSocketClient rsocketClient() {
		return delegate.rsocketClient();
	}

	@Override
	public MimeType dataMimeType() {
		return delegate.dataMimeType();
	}

	@Override
	public MimeType metadataMimeType() {
		return delegate.metadataMimeType();
	}

	@Override
	public RoutingRequestSpec route(String route, Object... routeVars) {
		String expandedRoute = expand(route, routeVars);
		RoutingRequestSpec requestSpec = new RoutingRequestSpec(delegate.route(route, routeVars), properties
				.isFailIfMissingRoutingMetadata(), expandedRoute);

		// needs to be expanded with routeVars
		RouteMatcher.Route parsed = routeMatcher.parseRoute(expandedRoute);

		properties.getAddress().entrySet().stream()
				.filter(entry -> routeMatcher.match(entry.getKey(), parsed)).findFirst()
				.ifPresent(entry -> {
					Map<? extends Key, String> tags = entry.getValue();
					Address.Builder address = address(routeMatcher, parsed,
							properties.getRouteId(), entry.getKey(), tags);

					requestSpec.metadata(address.build(),
							MimeTypes.ROUTING_FRAME_MIME_TYPE);
				});

		return requestSpec;
	}

	/* for testing */
	static Address.Builder address(RouteMatcher routeMatcher,
			RouteMatcher.Route route, Id originRouteId, String routeKey,
			Map<? extends Key, String> tags) {
		Map<String, String> extracted = routeMatcher.matchAndExtract(routeKey, route);
		Address.Builder address = Address.from(originRouteId);

		tags.forEach((tagKey, value) -> {
			if (tagKey.getWellKnownKey() != null) {
				address.with(tagKey.getWellKnownKey(), expand(value, extracted));
			}
			else if (tagKey.getKey() != null) {
				address.with(tagKey.getKey(), expand(value, extracted));
			}
		});
		return address;
	}

	@Override
	public RoutingRequestSpec metadata(Object metadata, MimeType mimeType) {
		return new RoutingRequestSpec(delegate.metadata(metadata, mimeType), properties
				.isFailIfMissingRoutingMetadata(), "unknown route");
	}

	/* for testing */
	static String expand(String route, Object... routeVars) {
		if (ObjectUtils.isEmpty(routeVars)) {
			return route;
		}
		StringBuffer sb = new StringBuffer();
		int index = 0;
		Matcher matcher = NAMES_PATTERN.matcher(route);
		while (matcher.find()) {
			Assert.isTrue(index < routeVars.length,
					() -> "No value for variable '" + matcher.group(1) + "'");
			String value = routeVars[index].toString();
			value = value.contains(".") ? value.replaceAll("\\.", "%2E") : value;
			matcher.appendReplacement(sb, value);
			index++;
		}
		return sb.toString();
	}

	/* for testing */
	static String expand(String template, Map<String, ?> vars) {
		if (template == null) {
			return null;
		}
		if (template.indexOf('{') == -1) {
			return template;
		}
		if (template.indexOf(':') != -1) {
			template = sanitizeSource(template);
		}

		if (ObjectUtils.isEmpty(vars)) {
			return template;
		}

		StringBuffer sb = new StringBuffer();
		Matcher matcher = NAMES_PATTERN.matcher(template);
		while (matcher.find()) {
			String match = matcher.group(1);
			String varName = getVariableName(match);
			Object varValue = vars.get(varName);
			String formatted = getVariableValueAsString(varValue);
			matcher.appendReplacement(sb, formatted);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Remove nested "{}" such as in URI vars with regular expressions.
	 */
	private static String sanitizeSource(String source) {
		int level = 0;
		StringBuilder sb = new StringBuilder();
		for (char c : source.toCharArray()) {
			if (c == '{') {
				level++;
			}
			if (c == '}') {
				level--;
			}
			if (level > 1 || (level == 1 && c == '}')) {
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static String getVariableName(String match) {
		int colonIdx = match.indexOf(':');
		return (colonIdx != -1 ? match.substring(0, colonIdx) : match);
	}

	private static String getVariableValueAsString(@Nullable Object variableValue) {
		return (variableValue != null ? variableValue.toString() : "");
	}

	public class RoutingRequestSpec implements RequestSpec {

		private final RequestSpec delegate;
		private final boolean failIfMissingRoutingMetadata;
		private final String route;
		private boolean hasRoutingMetadata;

		public RoutingRequestSpec(RequestSpec delegate, boolean failIfMissingRoutingMetadata, String route) {
			this.delegate = delegate;
			this.failIfMissingRoutingMetadata = failIfMissingRoutingMetadata;
			this.route = route;
		}

		public RoutingRequestSpec address(String serviceName) {
			delegate.metadata(spec -> {
				Address address = Address.from(properties.getRouteId())
						.with(WellKnownKey.SERVICE_NAME, serviceName).build();
				spec.metadata(address, MimeTypes.ROUTING_FRAME_MIME_TYPE);
			});
			return this;
		}

		public RoutingRequestSpec address(Consumer<Address.Builder> builderConsumer) {
			delegate.metadata(spec -> {
				Address.Builder builder = Address.from(properties.getRouteId());
				builderConsumer.accept(builder);
				spec.metadata(builder.build(), MimeTypes.ROUTING_FRAME_MIME_TYPE);
			});
			return this;
		}

		@Override
		public RoutingRequestSpec metadata(Consumer<MetadataSpec<?>> configurer) {
			// rather than call delegate, call configurer directly with this
			// so metadata(Object metadata, MimeType mimeType) from this
			// will be called instead to set hasRoutingMetadata properly.
			configurer.accept(this);
			return this;
		}

		@Override
		public Mono<Void> sendMetadata() {
			validateMetadataSet();
			return delegate.sendMetadata();
		}

		@Override
		public RetrieveSpec data(Object data) {
			delegate.data(data);
			return this;
		}

		@Override
		public RetrieveSpec data(Object producer, Class<?> elementClass) {
			delegate.data(producer, elementClass);
			return this;
		}

		@Override
		public RetrieveSpec data(Object producer, ParameterizedTypeReference<?> elementTypeRef) {
			delegate.data(producer, elementTypeRef);
			return this;
		}

		@Override
		public RoutingRequestSpec metadata(Object metadata, MimeType mimeType) {
			if (mimeType.equals(MimeTypes.ROUTING_FRAME_MIME_TYPE)) {
				hasRoutingMetadata = true;
			}
			delegate.metadata(metadata, mimeType);
			return this;
		}

		@Override
		public Mono<Void> send() {
			validateMetadataSet();
			return delegate.send();
		}

		@Override
		public <T> Mono<T> retrieveMono(Class<T> dataType) {
			validateMetadataSet();
			return delegate.retrieveMono(dataType);
		}

		@Override
		public <T> Mono<T> retrieveMono(ParameterizedTypeReference<T> dataTypeRef) {
			validateMetadataSet();
			return delegate.retrieveMono(dataTypeRef);
		}

		@Override
		public <T> Flux<T> retrieveFlux(Class<T> dataType) {
			validateMetadataSet();
			return delegate.retrieveFlux(dataType);
		}

		@Override
		public <T> Flux<T> retrieveFlux(ParameterizedTypeReference<T> dataTypeRef) {
			validateMetadataSet();
			return delegate.retrieveFlux(dataTypeRef);
		}

		private void validateMetadataSet() {
			if (failIfMissingRoutingMetadata && !hasRoutingMetadata) {
				throw new IllegalArgumentException(MimeTypes.ROUTING_FRAME_MIME_TYPE + " metadata was not set for route: " + route);
			}
		}
	}

}
