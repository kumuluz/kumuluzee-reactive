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

import com.kumuluz.ee.reactive.common.annotations.ReactiveEventListener;
import com.kumuluz.ee.reactive.common.utils.EventListenerFactory;
import com.kumuluz.ee.reactive.common.utils.EventListenerInitExtension;
import com.kumuluz.ee.reactive.common.utils.EventListenerInstance;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Implementation of EventListenerInitExtension interface
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class VertxEventListenerInitExtension implements EventListenerInitExtension {

	private static final Logger log = Logger.getLogger(VertxEventListenerInitExtension.class.getName());
	
	EventListenerFactory<VertxEventListenerRunnable> vertxEventListenerFactory;
	
	@Override
	public <X> void after(@Observes AfterDeploymentValidation adv, BeanManager bm) {
		
		vertxEventListenerFactory = new VertxEventListenerFactory();
		
		for(EventListenerInstance listenerInstance : instanceList) {
			log.info(listenerInstance.getMethod().getName());
		}
		
		if(instanceList.size() > 0) {
			ExecutorService executor = Executors.newFixedThreadPool(instanceList.size());
			
			for(EventListenerInstance listenerInstance : instanceList) {
				ReactiveEventListener annotation = listenerInstance.getAnnotation();
				Method method = listenerInstance.getMethod();
				
				String address = annotation.address();
				
				Object instance = bm.getReference(
						listenerInstance.getBean(),
						method.getDeclaringClass(),
						bm.createCreationalContext(listenerInstance.getBean()));
				
				VertxEventListenerRunnable vertxEventListenerRunnable = vertxEventListenerFactory.createEventListener(instance, address, method);
				
				if(vertxEventListenerRunnable != null) {
					executor.submit(vertxEventListenerRunnable);
					
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {							
							executor.shutdown();
							
							try {
								executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
							} catch (Exception e) {
								log.warning(e.getLocalizedMessage());
							}
						}
					});
				}
				
			}
		}
	}

}
