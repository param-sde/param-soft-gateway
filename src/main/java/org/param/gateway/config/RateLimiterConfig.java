/*
 * Copyright (c) 2024 PARAM SOFT. All rights reserved.
 * 
 * This software and its documentation (the "Software") are confidential and proprietary to PARAM SOFT.
 * The Software is protected by copyright, trade secret, and other intellectual property laws. 
 * Unauthorized use, reproduction, modification, distribution, or disclosure of the Software, 
 * in whole or in part, is strictly prohibited without prior written consent from PARAM SOFT.
 * The Software is provided "as-is" without any express or implied warranty of any kind, 
 * including but not limited to the warranties of merchant ability, fitness for a particular purpose, 
 * or non infringement. You may use the Software only in accordance with the terms of 
 * the applicable license agreement. 
 *
 * For more information, including licensing inquiries or support, 
 * please contact: PARAM SOFT - https://www.paramsoft.org
 */
package org.param.gateway.config;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Author: PARAMESHWARAN PV
 * Date: 11-Apr-2025 : 4:24:25 PM
 * Since: 1.0.0
 * @See #
 */

@Configuration
public class RateLimiterConfig {

	private static final Logger log = LoggerFactory.getLogger(RateLimiterConfig.class);

	/*-
	@Bean
	public KeyResolver dynamicServiceKeyResolver() {
		return exchange -> {
			String path = exchange.getRequest().getURI().getPath();
			String service = Arrays.stream(path.split("/")).filter(part -> !part.isBlank()).findFirst()
					.orElse("default");
	
			String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
					.map(addr -> addr.getAddress().getHostAddress()).orElse("anonymous");
	
			return Mono.just(service + ":" + clientIp);
		};
	}
	*/

	@Bean
	public KeyResolver dynamicServiceKeyResolver() {
		return exchange -> {
			String service = extractFirstPathSegment(exchange);
			String clientIp = resolveClientIp(exchange);
			String key = service + ":" + clientIp;

			log.info("RateLimiter Key: [{}] for path [{}]", key, exchange.getRequest().getPath());
			return Mono.just(key);
		};
	}

	private String extractFirstPathSegment(ServerWebExchange exchange) {
		String path = exchange.getRequest().getPath().value();

		return Arrays.stream(path.split("/")).filter(segment -> !segment.isBlank()).findFirst().orElse("default");
	}

	private String resolveClientIp(ServerWebExchange exchange) {
		String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			String ip = xForwardedFor.split(",")[0].trim();
			log.debug("Resolved IP from X-Forwarded-For: [{}]", ip);
			return ip;
		}

		InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
		String fallbackIp = Optional.ofNullable(remoteAddress).map(addr -> addr.getAddress().getHostAddress())
				.orElse("anonymous");

		log.debug("Resolved IP from RemoteAddress: [{}]", fallbackIp);
		return fallbackIp;
	}
}
