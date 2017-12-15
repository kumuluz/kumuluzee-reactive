/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.reactive.vertx.discovery;

/**
 * Representation of a service as used by Vert.x Service Discovery
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class VertxService {

	private String name;
	private String version;
	private String environment;
	private long ttl;
	private long pingInterval;
	private String baseUrl;
	private String id;
	
	public VertxService(String name, String version, String environment, long ttl, long pingInterval,
			String baseUrl, String id) {
		super();
		this.name = name;
		this.version = version;
		this.environment = environment;
		this.ttl = ttl;
		this.pingInterval = pingInterval;
		this.baseUrl = baseUrl;
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getEnvironment() {
		return environment;
	}
	
	public long getTtl() {
		return ttl;
	}
	
	public long getPingInterval() {
		return pingInterval;
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
	public String getId() {
		return id;
	}
	
}
