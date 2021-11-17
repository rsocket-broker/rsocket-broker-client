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
import java.util.UUID;

public class Id {
	private final long first;
	private final long second;

	public Id(long first, long second) {
		this.first = first;
		this.second = second;
	}

	public long getFirst() {
		return this.first;
	}

	public long getSecond() {
		return this.second;
	}

	@Override
	public String toString() {
		return "Id{" + new UUID(first, second) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Id id = (Id) o;
		return this.first == id.first &&
				this.second == id.second;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.first, this.second);
	}

	public static Id from(long[] parts) {
		if (parts == null || parts.length != 2) {
			throw new IllegalArgumentException("parts must have a length of 2");
		}
		return new Id(parts[0], parts[1]);
	}

	// userful convention for serialization
	public static Id valueOf(String uuid) {
		return from(uuid);
	}

	public static Id from(String uuid) {
		return from(UUID.fromString(uuid));
	}

	public static Id from(UUID uuid) {
		Objects.requireNonNull(uuid, "uuid may not be null");
		return new Id(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
	}

	public static Id random() {
		return from(UUID.randomUUID());
	}
}
