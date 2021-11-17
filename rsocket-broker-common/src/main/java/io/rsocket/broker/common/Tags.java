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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class Tags {

	private static final Tags EMPTY = builder().buildTags();

	private final Map<Key, String> tags;

	public Tags(Map<Key, String> tags) {
		Objects.requireNonNull(tags, "tags may not be null");
		this.tags = tags;
	}

	public Map<Key, String> asMap() {
		return this.tags;
	}

	public String get(WellKnownKey key) {
		return tags.get(Key.of(key));
	}

	public String get(String key) {
		return tags.get(Key.of(key));
	}

	public boolean containsKey(WellKnownKey key) {
		return tags.containsKey(Key.of(key));
	}

	public boolean containsKey(String key) {
		return tags.containsKey(Key.of(key));
	}

	public Set<Entry<Key, String>> entries() {
		return this.tags.entrySet();
	}

	public boolean isEmpty() {
		return this.tags.isEmpty();
	}

	@Override
	public String toString() {
		return Tags.class.getSimpleName() + tags.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Tags tags1 = (Tags) o;
		return this.tags.equals(tags1.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.tags);
	}

	@SuppressWarnings("rawtypes")
	public static Builder<?> builder() {
		return new Builder();
	}

	public static Tags empty() {
		return EMPTY;
	}

	public static class Builder<SELF extends Builder<SELF>> {

		private final Map<Key, String> tags = new LinkedHashMap<>();

		protected Builder() {
		}

		public SELF with(String key, String value) {
			Objects.requireNonNull(key, "key may not be null");
			if (key.length() > 128) {
				throw new IllegalArgumentException("key length can not be greater than 128, was " + key
						.length());
			}
			return with(Key.of(key), value);
		}

		public SELF with(WellKnownKey key, String value) {
			Objects.requireNonNull(key, "key may not be null");
			return with(Key.of(key), value);
		}

		@SuppressWarnings("unchecked")
		public SELF with(Key key, String value) {
			Objects.requireNonNull(key, "key may not be null");
			if (value != null && value.length() > 128) {
				throw new IllegalArgumentException("value length can not be greater than 128, was " + value
						.length());
			}
			this.tags.put(key, value);
			return (SELF) this;
		}

		@SuppressWarnings("unchecked")
		public SELF with(Tags tags) {
			this.tags.putAll(tags.asMap());
			return (SELF) this;
		}

		protected Map<Key, String> getTags() {
			return this.tags;
		}

		public Tags buildTags() {
			return new Tags(Collections.unmodifiableMap(this.tags));
		}

	}

}
