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

package io.rsocket.routing.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Tags {

	private final Map<Key, String> tags;

	public Tags(Map<Key, String> tags) {
		Objects.requireNonNull(tags, "tags may not be null");
		this.tags = tags;
	}

	public Map<Key, String> asMap() {
		return this.tags;
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

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Map<Key, String> tags = new LinkedHashMap<>();

		private Builder() {
		}

		public Builder with(String key, String value) {
			Objects.requireNonNull(key, "key may not be null");
			if (key.length() > 128) {
				throw new IllegalArgumentException("key length can not be greater than 128, was " + key
						.length());
			}
			return with(Key.of(key), value);
		}

		public Builder with(WellKnownKey key, String value) {
			Objects.requireNonNull(key, "key may not be null");
			return with(Key.of(key), value);
		}

		public Builder with(Key key, String value) {
			Objects.requireNonNull(key, "key may not be null");
			if (value != null && value.length() > 128) {
				throw new IllegalArgumentException("value length can not be greater than 128, was " + value
						.length());
			}
			this.tags.put(key, value);
			return this;
		}

		public Builder with(Tags tagsMetadata) {
			this.tags.putAll(tagsMetadata.asMap());
			return this;
		}

		public Tags build() {
			return new Tags(Collections.unmodifiableMap(this.tags));
		}

	}

}
