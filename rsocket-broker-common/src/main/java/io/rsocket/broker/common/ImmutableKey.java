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

import java.util.Objects;
import java.util.StringJoiner;

public class ImmutableKey implements Key {

	private final WellKnownKey wellKnownKey;

	private final String key;

	public ImmutableKey(WellKnownKey wellKnownKey) {
		this(wellKnownKey, null);
	}

	public ImmutableKey(String key) {
		this(null, key);
	}

	private ImmutableKey(WellKnownKey wellKnownKey, String key) {
		this.wellKnownKey = wellKnownKey;
		this.key = key;
	}

	public WellKnownKey getWellKnownKey() {
		return this.wellKnownKey;
	}

	public String getKey() {
		return this.key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Key key1 = (Key) o;
		return this.wellKnownKey == key1.getWellKnownKey()
				&& Objects.equals(this.key, key1.getKey());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.wellKnownKey, this.key);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "[", "]");
		if (wellKnownKey != null) {
			joiner.add(wellKnownKey.toString());
			joiner.add(String.format("0x%02x", wellKnownKey.getIdentifier()));
		}
		if (key != null) {
			joiner.add("'" + key + "'");
		}
		return joiner.toString();
	}
}
