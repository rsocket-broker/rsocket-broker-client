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

package io.rsocket.routing.client.spring;

import io.rsocket.metadata.WellKnownMimeType;

import org.springframework.util.MimeType;

/**
 * Holds MimeType objects for RSocket mime types.
 */
public abstract class MimeTypes {

	/**
	 * Routing Frame mime type.
	 */
	public static final MimeType ROUTING_FRAME_MIME_TYPE = new MimeType(io.rsocket.routing.client.MimeTypes.MESSAGE_TYPE,
			io.rsocket.routing.client.MimeTypes.ROUTING_FRAME_SUBTYPE);
	public static final String ROUTING_FRAME_METADATA_KEY = "routingframe";

	public static final MimeType COMPOSITE_MIME_TYPE = MimeType
			.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.toString());

	private MimeTypes() {

	}


}
