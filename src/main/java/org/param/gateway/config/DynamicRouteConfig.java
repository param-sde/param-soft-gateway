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

import java.net.URI;
import java.util.List;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: PARAMESHWARAN PV
 * Date: 27-Jun-2025 : 10:08:51 AM
 * Since: 1.0.0
 * @See #
 */

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamicRouteConfig {

	private final ReactiveDiscoveryClient discoveryClient;

	// @formatter:off
 
	@Bean
	public RouteDefinitionLocator dynamicRouteDefinitionLocator() {
	    return () -> discoveryClient.getServices()
	            .filter(serviceId -> serviceId.startsWith("param-soft-"))
	            .map(serviceId -> {
	            	
	                RouteDefinition routeDefinition = new RouteDefinition();
	                routeDefinition.setId("route-" + serviceId);
	                routeDefinition.setUri(URI.create("lb://" + serviceId.toUpperCase()));

	                PredicateDefinition predicate = new PredicateDefinition();
	                predicate.setName("Path");
	                predicate.addArg("pattern", "/" + serviceId + "/**");

	                FilterDefinition filter = new FilterDefinition();
	                filter.setName("RewritePath");
	                filter.addArg("regexp", "/" + serviceId + "/(?<segment>.*)");
	                filter.addArg("replacement", "/${segment}");

	                routeDefinition.setPredicates(List.of(predicate));
	                routeDefinition.setFilters(List.of(filter));

	                log.info("Registered dynamic route for service: {}", serviceId);
	                return routeDefinition;
	            });
	}
	
	// @formatter:on

}