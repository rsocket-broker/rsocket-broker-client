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

public class MutableKey implements Key {

	private WellKnownKey wellKnownKey;

	private String key;

	public MutableKey() {
		System.out.println("here");
	}

	public MutableKey(String text) {
		if (text != null && !text.isEmpty()) {
			try {
				wellKnownKey = WellKnownKey.valueOf(text.toUpperCase());
			}
			catch (IllegalArgumentException e) {
				// NOT a valid well know key
				key = text;
			}
		}
	}

	public static MutableKey of(WellKnownKey key) {
		MutableKey mutableKey = new MutableKey();
		mutableKey.setWellKnownKey(key);
		return mutableKey;
	}

	public static MutableKey of(String key) {
		return new MutableKey(key);
	}

	public WellKnownKey getWellKnownKey() {
		return wellKnownKey;
	}

	public void setWellKnownKey(WellKnownKey wellKnownKey) {
		this.wellKnownKey = wellKnownKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MutableKey tag = (MutableKey) o;
		return wellKnownKey == tag.wellKnownKey
				&& Objects.equals(key, tag.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(wellKnownKey, key);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "[", "]");
		if (wellKnownKey != null) {
			joiner.add(wellKnownKey.name());
		}
		if (key != null) {
			joiner.add("'" + key + "'");
		}
		return joiner.toString();
	}

}