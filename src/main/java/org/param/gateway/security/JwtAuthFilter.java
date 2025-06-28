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
package org.param.gateway.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Author: PARAMESHWARAN PV
 * Date: 27-Jun-2025 : 8:19:00 AM
 * Since: 1.0.0
 * @See #
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

	@Value("#{'${gateway.security.skip-paths}'.split(',')}")
	private List<String> skipPaths;

	private final JwtUtil jwtUtil;
	private final AntPathMatcher pathMatcher;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange,
			org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();
		log.debug("Incoming request path: {}", path);

		for (String skipPath : skipPaths) {
			if (pathMatcher.match(skipPath.trim(), path)) {
				log.debug("Skipping JWT validation for path: {}", path);
				return chain.filter(exchange);
			}
		}

		log.debug("Performing JWT validation for path: {}", path);

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return this.onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);

		if (!jwtUtil.isTokenValid(token)) {
			return this.onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
		}

		var claims = jwtUtil.validateTokenAndGetClaims(token);
		var username = claims.getSubject();

		List<String> roles = extractStringListClaim(claims, "roles");

		ServerHttpRequest modifiedRequest = exchange.getRequest().mutate().header("X-User-Name", username)
				.header("X-User-Roles", String.join(",", roles)).build();

		return chain.filter(exchange.mutate().request(modifiedRequest).build());
	}

	private List<String> extractStringListClaim(Claims claims, String key) {
		Object value = claims.get(key);
		if (value instanceof List<?>) {
			return ((List<?>) value).stream().map(String::valueOf).collect(Collectors.toList());
		}
		throw new IllegalArgumentException("Claim '" + key + "' is not a list.");
	}

	private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
		log.warn("JWT Filter error: {}", message);
		exchange.getResponse().setStatusCode(status);
		return exchange.getResponse().setComplete();
	}

	@Override
	public int getOrder() {
		return -1;
	}
}