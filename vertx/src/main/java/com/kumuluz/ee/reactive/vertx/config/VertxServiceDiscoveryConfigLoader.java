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
package com.kumuluz.ee.reactive.vertx.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import io.vertx.core.json.JsonObject;

/**
 * Util class for getting initialization parameters for Vert.x Service Discovery.
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class VertxServiceDiscoveryConfigLoader {
	
	public static final String PREFIX = "kumuluzee.reactive.vertx.discovery.";
	public static final String ENV = "kumuluzee.env.name";
	
	public static JsonObject getConfiguration() {
		ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
		String env = null;
		long ttl = -1;
		long pingInterval = -1;
		
		env = configurationUtil.get(PREFIX + "env.name").orElse(null);
		ttl = configurationUtil.getLong(PREFIX + "ttl").orElse((long) 30);
		pingInterval = configurationUtil.getLong(PREFIX + "ping-interval").orElse((long) 20);
		
		if (env == null) {
			env = configurationUtil.get(ENV).orElse("dev");
		}
		
		JsonObject conf = new JsonObject()
				.put("env", env)
				.put("ttl", ttl)
				.put("ping-interval", pingInterval);
		
		return conf;
	}
}
