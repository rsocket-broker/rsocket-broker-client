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

public interface Tag extends Comparable<Tag> {
  Key getKey();

  String getValue();

  static Tag of(Key key, String value) {
    return new ImmutableTag(key, value);
  }

  static Tag of(String key, String value) {
    return new ImmutableTag(key, value);
  }

  @Override
  default int compareTo(Tag t) {
    Key k1 = getKey();
    Key k2 = t.getKey();
    return k1.getKey().compareTo(k2.getKey());
  }
}
