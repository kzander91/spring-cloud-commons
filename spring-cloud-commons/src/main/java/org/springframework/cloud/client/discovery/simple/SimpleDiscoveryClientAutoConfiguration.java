/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client.discovery.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Spring Boot auto-configuration for simple properties-based discovery client.
 *
 * @author Biju Kunjummen
 * @author Charu Covindane
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ NoopDiscoveryClientAutoConfiguration.class,
		CommonsClientAutoConfiguration.class })
public class SimpleDiscoveryClientAutoConfiguration
		implements ApplicationListener<WebServerInitializedEvent> {

	private ServerProperties server;

	private InetUtils inet;

	private int port = 0;

	private SimpleDiscoveryProperties simple = new SimpleDiscoveryProperties();

	@Autowired(required = false)
	public void setServer(ServerProperties server) {
		this.server = server;
	}

	@Autowired
	public void setInet(InetUtils inet) {
		this.inet = inet;
	}

	@Bean
	@ConditionalOnMissingBean
	public SimpleDiscoveryProperties simpleDiscoveryProperties(
			@Value("${spring.application.name:application}") String serviceId) {
		simple.getLocal().setServiceId(serviceId);
		simple.getLocal().setHost(this.inet.findFirstNonLoopbackHostInfo().getHostname());
		simple.getLocal().setPort(findPort());

		return simple;
	}

	@Bean
	@Order
	public DiscoveryClient simpleDiscoveryClient(SimpleDiscoveryProperties properties) {
		return new SimpleDiscoveryClient(properties);
	}

	private int findPort() {
		if (port > 0) {
			return port;
		}
		if (this.server != null && this.server.getPort() != null
				&& this.server.getPort() > 0) {
			return this.server.getPort();
		}
		return 8080;
	}

	@Override
	public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
		this.port = webServerInitializedEvent.getWebServer().getPort();
		if (this.port > 0) {
			simple.getLocal().setPort(this.port);
		}
	}

}
