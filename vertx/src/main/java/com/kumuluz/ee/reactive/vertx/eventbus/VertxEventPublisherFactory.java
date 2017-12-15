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

import com.kumuluz.ee.reactive.common.utils.EventPublisherFactory;
import com.kumuluz.ee.reactive.vertx.utils.VertxUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageProducer;

import javax.enterprise.context.RequestScoped;

/**
 * Implementation of EventPublisherFactory<T> inteface.
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
@RequestScoped
public class VertxEventPublisherFactory implements EventPublisherFactory<MessageProducer<Object>> {
	
	@Override
	public MessageProducer<Object> createEventPublisher(String address) {
		
		MessageProducer<Object> messageProducer = null;
		
		EventBus eventBus = VertxUtils.getInstance().getVertx().eventBus();
		
		messageProducer = eventBus.publisher(address);
				
		return messageProducer;
	}

}
