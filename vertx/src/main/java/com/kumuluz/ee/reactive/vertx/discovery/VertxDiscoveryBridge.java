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

import com.kumuluz.ee.common.runtime.EeRuntime;
import com.kumuluz.ee.common.runtime.EeRuntimeExtension;
import com.kumuluz.ee.discovery.utils.DiscoveryUtil;
import com.kumuluz.ee.reactive.vertx.config.VertxServiceDiscoveryConfigLoader;
import com.kumuluz.ee.reactive.vertx.utils.VertxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service Discovery bridge for handling service requests and service
 * (de)registration using KumuluzEE Discovery
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
@ApplicationScoped
public class VertxDiscoveryBridge {

	private static final Logger log = Logger.getLogger(VertxDiscoveryBridge.class.getName());
	
	private static final String EXTENSION_GROUP = "discovery";
	private static final String STATUS_UP = "UP";
	private static final String STATUS_DOWN = "DOWN";
	private static final String ANNOUNCE_ADDRESS = "vertx.discovery.announce";
	private static final String REQUEST_ADDRESS = "vertx.discovery.request";

	private List<VertxService> services = new ArrayList<VertxService>();
	private ServiceDiscovery discovery;
	private Vertx vertx;
	private String env;
	private long ttl;
	private long pingInterval;
	
	@Inject
    private DiscoveryUtil discoveryUtil;

	public VertxDiscoveryBridge() {
		
	}	
	
	void init (@Observes @Initialized(ApplicationScoped.class) Object init) {	
		List<EeRuntimeExtension> extensions = EeRuntime.getInstance().getEeExtensions();
		for(EeRuntimeExtension extension : extensions) {
			if (extension.getGroup().equals(EXTENSION_GROUP)) {
				initializeServiceDiscoveryBridge();
				log.info("Initializing Vert.x Service Discovery bridge.");
				break;
			}
		}
	}
	
	private void initializeServiceDiscoveryBridge() {
		while (VertxUtils.getInstance().getVertx() == null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.severe(e.getLocalizedMessage());
			}
		}
		vertx = VertxUtils.getInstance().getVertx();
		loadConfig();
	}
	
	private void loadConfig() {
		JsonObject conf = VertxServiceDiscoveryConfigLoader.getConfiguration();
		
		env = conf.getString("env");
		ttl = conf.getLong("ttl");
		pingInterval = conf.getLong("ping-interval");
		
		initializeServiceDiscovery();
	}
	
	private void initializeServiceDiscovery() {		
		discovery = VertxUtils.getInstance().getServiceDiscovery();
		
		handleAnnouncements();

		handleRequests();

		initialServicesCheck();

		log.info("Vert.x Service Discovery bridge initialized.");
	}

	private void handleAnnouncements() {
		vertx.eventBus().consumer(ANNOUNCE_ADDRESS, ar -> {
			JsonObject service = (JsonObject) ar.body();
			handleService(service);
		});
	}

	private void handleRequests() {
		vertx.eventBus().consumer(REQUEST_ADDRESS, ar -> {
			vertx.executeBlocking(future -> {
				Optional<String> serviceUrl = getServiceUrl((JsonObject) ar.body());

				if (serviceUrl.isPresent()) {
					JsonObject reply = new JsonObject()
							.put("status", 200)
							.put("baseUrl", serviceUrl.get());
					ar.reply(reply);
				} else {
					JsonObject reply = new JsonObject()
							.put("status", 404);
					ar.reply(new JsonObject());
				}
			}, null);
		});
	}

	private Optional<String> getServiceUrl(JsonObject request) {
		String name = request.getString("name");
		String version = request.getString("version", "1.0.0");
		String env = request.getString("env", "dev");

		List<VertxService> services = this.services.stream()
				.filter(service -> service.getEnvironment().equals(env)
						&& service.getName().equals(name) && service.getVersion().equals(version))
				.collect(Collectors.toList());

		if (services.size() > 0) {
			return Optional.of(services.get(ThreadLocalRandom.current().nextInt(0, services.size()))
					.getBaseUrl());
		} else {
			Optional<URL> url = discoveryUtil.getServiceInstance(name, version, env);
			if (url.isPresent()) {
				return Optional.of(url.get().toString());
			}
		}

		return Optional.empty();
	}

	private void initialServicesCheck() {
		discovery.getRecords((JsonObject) null, ar -> {
			if (ar.succeeded() && ar.result() != null) {
				List<Record> records = ar.result();
				registerServices(records);
			}
		});
	}
	
	private void registerServices(List<Record> records) {
		for (Record record : records) {
			if (!isImported(record.getRegistration())) {
				importService(record);
			}
		}
	}
	
	private void deregisterService(String name, String baseUrl, List<Record> records) {
		List<VertxService> filteredServices = services.stream()
				.filter(service -> service.getBaseUrl().equals(baseUrl) && service.getName().equals(name))
				.collect(Collectors.toList());
		
		for (VertxService service : filteredServices) {
			if (wasRemoved(service, records)) {
				services.remove(service);
				
				discoveryUtil.deregister(service.getId());
				break;
			}
		}
	}
	
	private boolean wasRemoved(VertxService service, List<Record> records) {
		for (Record record : records) {
			if(record.getRegistration().equals(service.getId())) {
				return false;
			}
		}		
		return true;
	}
	
	private void handleService(JsonObject service) {
		String name = service.getString("name");
		String baseUrl = service.getJsonObject("location").getString("endpoint");
		String status = service.getString("status");
		
		discovery.getRecords(r -> r.getName().equals(name) 
				&& r.getLocation().getString("endpoint").equals(baseUrl), ar -> {
			if (ar.succeeded() && ar.result() != null) {
				
				List<Record> records = ar.result();
				
				vertx.executeBlocking(future -> {
					if (status.equals(STATUS_UP)) {
						registerServices(records);
					} else if (status.equals(STATUS_DOWN)) {
						deregisterService(name, baseUrl, records);
					}		
				}, null);
						
			}
		});
	}
	
	private void importService(Record record) {
		String name = record.getName();
		String baseUrl = record.getLocation().getString("endpoint");
		String id = record.getRegistration();
		String version = record.getMetadata().getString("version", "1.0.0");
		long ttl = record.getMetadata().getLong("ttl", this.ttl);
		long pingInterval = record.getMetadata().getLong("ping-interval", this.pingInterval);
		String environment = record.getMetadata().getString("env", this.env);
		
		VertxService service = new VertxService(name, version, environment, ttl, pingInterval, baseUrl, id);
		services.add(service);
		
		discoveryUtil.register(
				name,
				version,
				environment,
				ttl,
				pingInterval,
				false,
				baseUrl,
				id);
	}
	
	private boolean isImported(String regId) {
		for (VertxService service : services) {
			if (service.getId().equals(regId))
				return true;
		}
		return false;
	}

}
