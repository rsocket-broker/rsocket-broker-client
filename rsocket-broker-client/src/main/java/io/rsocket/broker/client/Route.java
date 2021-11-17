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

package io.rsocket.broker.client;

import java.util.function.Consumer;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.broker.frames.Address;

public interface Route {
	ByteBufAllocator allocator();

	void encodeAddressMetadata(CompositeByteBuf metadataHolder, String serviceName);

	void encodeAddressMetadata(CompositeByteBuf metadataHolder, Consumer<Address.Builder> addressConsumer);
}
