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
package com.kumuluz.ee.reactive.vertx.utils;

import com.kumuluz.ee.reactive.vertx.config.VertxConfigLoader;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Util class for Vert.x and Vert.x Service Discovery initialization.
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class VertxUtils {
	
	static final Logger log = Logger.getLogger(VertxUtils.class.getName());
	
	private Vertx vertx;
	private ServiceDiscovery serviceDiscovery;
	
	private static VertxUtils instance;

	public VertxUtils() {
		instance = this;
		initializeVertx();
	}
	
	/**
	 * Method initializes Vert.x from configuration
	 */
	private void initializeVertx() {	
		VertxOptions vertxOptions = VertxConfigLoader.getVertxOptions();
		
		if(VertxConfigLoader.isClustered) {
			Vertx.clusteredVertx(vertxOptions, res -> {
				if(res.succeeded()) {
					this.vertx = res.result();
					log.info("Clustered Vert.x successfully initialized.");
				}
			});
		} else {
			this.vertx = Vertx.vertx(vertxOptions);
			log.info("Vert.x successfully initialized.");
		}		
	}
	
	public static VertxUtils getInstance() {
		return instance;
	}
	
	public Vertx getVertx() {
		return vertx;
	}
	
	public ServiceDiscovery getServiceDiscovery() {		
		if(serviceDiscovery == null) {
			serviceDiscovery = ServiceDiscovery.create(vertx);
			log.info("Vert.x Service Discovery initialized.");
		}
		return serviceDiscovery;
	}
	
	public void close(CompletableFuture<Boolean> completion) {
		vertx.close(res -> {
			if(res.succeeded()) {
				completion.complete(true);
			} else {
				completion.complete(false);
			}
		});
	}
	
}
