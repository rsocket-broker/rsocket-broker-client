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

package io.rsocket.routing.common.spring;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class TransportProperties {

	private CustomProperties custom;

	private TcpProperties tcp = new TcpProperties();

	private WebsocketProperties websocket;

	public CustomProperties getCustom() {
		return this.custom;
	}

	public void setCustom(CustomProperties custom) {
		this.custom = custom;
	}

	public TcpProperties getTcp() {
		return this.tcp;
	}

	public void setTcp(TcpProperties tcp) {
		this.tcp = tcp;
	}

	public WebsocketProperties getWebsocket() {
		return this.websocket;
	}

	public void setWebsocket(WebsocketProperties websocket) {
		this.websocket = websocket;
	}

	public boolean hasCustomTransport() {
		return custom != null;
	}

	public static abstract class HostPortProperties {

		/**
		 * Server port.
		 */
		private Integer port;

		/**
		 * Network address to which the server should bind.
		 */
		private String host;

		public Integer getPort() {
			return this.port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public String getHost() {
			return this.host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public InetAddress getHostAsAddress() {
			if (host == null) {
				return null;
			}
			try {
				return InetAddress.getByName(host);
			}
			catch (UnknownHostException ex) {
				throw new IllegalStateException("Unknown host " + host, ex);
			}
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", getClass()
					.getSimpleName() + "[", "]")
					.add("port=" + port)
					.add("host=" + host)
					.toString();
		}
	}

	public static class TcpProperties extends HostPortProperties {
	}

	public static class WebsocketProperties extends HostPortProperties {

		/**
		 * Path under which RSocket handles requests (only works with websocket
		 * transport).
		 */
		private String mappingPath;

		public String getMappingPath() {
			return this.mappingPath;
		}

		public void setMappingPath(String mappingPath) {
			this.mappingPath = mappingPath;
		}

		public URI getUri() {
			// TODO: validation, scheme
			return URI.create("ws://" + getHost() + ":" + getPort() + mappingPath);
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", WebsocketProperties.class.getSimpleName() + "[", "]")
					.add("address='" + getHost() + "'")
					.add("port='" + getPort() + "'")
					.add("mappingPath='" + mappingPath + "'")
					.toString();
		}
	}

	public static class CustomProperties {
		private String type;
		private Map<String,String> args = new HashMap<>();

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Map<String, String> getArgs() {
			return this.args;
		}

		public void setArgs(Map<String, String> args) {
			this.args = args;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", CustomProperties.class.getSimpleName() + "[", "]")
					.add("name='" + type + "'")
					.add("args=" + args)
					.toString();
		}
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", getClass()
				.getSimpleName() + "[", "]")
				.add("custom=" + getCustom())
				.add("tcp=" + getTcp())
				.add("websocket=" + getWebsocket())
				.toString();
	}
}
