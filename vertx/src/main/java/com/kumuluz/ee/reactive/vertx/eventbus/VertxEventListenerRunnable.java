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
package com.kumuluz.ee.reactive.vertx.eventbus;

import com.kumuluz.ee.reactive.vertx.utils.VertxUtils;
import io.vertx.core.eventbus.MessageConsumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Runnable for registering an event listener to the event bus.
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class VertxEventListenerRunnable implements Runnable {

	private static final Logger log = Logger.getLogger(VertxEventListenerRunnable.class.getName());

	private MessageConsumer<Object> messageConsumer;
	private String address;
	private Method method;
	private Object instance;

	public VertxEventListenerRunnable(String address, Method method, Object instance) {
		this.address = address;
		this.method = method;
		this.instance = instance;
	}

	@Override
	public void run() {
		VertxUtils vertxUtils = VertxUtils.getInstance();

		while (vertxUtils.getVertx() == null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.warning("Error at waiting for Vert.x to initialize. " + e.getLocalizedMessage());
			}
		}

		log.info("Configuring MessageConsumer for address: " + address + ".");
		
		messageConsumer = vertxUtils.getVertx().eventBus().consumer(address);

		messageConsumer.handler(message -> {
			if (message.body() != null) {
				try {
					method.invoke(instance, message);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.warning("Error at invoking consumer for address " + address + ". " + e.getLocalizedMessage());
				}
			}
		});
	}

}
