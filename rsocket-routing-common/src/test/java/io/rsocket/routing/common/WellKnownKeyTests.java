/*
 * Copyright 2021 the original author or authors.
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

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.fail;

public class WellKnownKeyTests {

	@Test
	void noDuplicateIds() {
		HashMap<Byte, WellKnownKey> keysById = new HashMap<>();
		for (WellKnownKey key : WellKnownKey.values()) {
			if (keysById.containsKey(key.getIdentifier())) {
				WellKnownKey existing = keysById.get(key.getIdentifier());
				fail(String.format("Duplicate id %d for already exists for key %s:%s. Duplicate key %s:%s",
						key.getIdentifier(), existing.name(), existing.getString(), key.name(), key.getString()));
			}
			keysById.put(key.getIdentifier(), key);
		}
	}

}
