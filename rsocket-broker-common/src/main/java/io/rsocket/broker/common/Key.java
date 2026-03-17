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

package io.rsocket.broker.common;

/**
 * Represents a tag key that can be either a {@link WellKnownKey} (compact byte identifier)
 * or a custom string key. Use {@link #of(String)} or {@link #of(WellKnownKey)} to create instances.
 */
public interface Key {
	WellKnownKey getWellKnownKey();

	String getKey();

	static Key of(String key) {
		return new ImmutableKey(key);
	}

	static Key of(WellKnownKey key) {
		return key.getKey();
	}
}
