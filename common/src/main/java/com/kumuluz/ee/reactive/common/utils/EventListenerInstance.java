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
package com.kumuluz.ee.reactive.common.utils;

import com.kumuluz.ee.reactive.common.annotations.ReactiveEventListener;

import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Method;

/**
 * Representation of a bean, which contains ReactiveEventListener annotation.
 *
 * @author Žan Ožbot
 * @since 1.0.0
 */
public class EventListenerInstance {
	
	private Bean<?> bean;
	private ReactiveEventListener annotation;
	private Method method;
	
	public EventListenerInstance(Bean<?> bean, Method method, ReactiveEventListener annotation) {
		this.bean = bean;
		this.method = method;
		this.annotation = annotation;
	}

	public Bean<?> getBean() {
		return bean;
	}

	public void setBean(Bean<?> bean) {
		this.bean = bean;
	}
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public ReactiveEventListener getAnnotation() {
		return annotation;
	}

	public void setAnnotation(ReactiveEventListener annotation) {
		this.annotation = annotation;
	}	
	
}
