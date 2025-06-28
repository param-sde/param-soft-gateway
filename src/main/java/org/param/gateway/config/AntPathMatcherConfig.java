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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/**
 * Author: PARAMESHWARAN PV
 * Date: 27-Jun-2025 : 9:56:27 AM
 * Since: 1.0.0
 * @See #
 */

@Configuration
public class AntPathMatcherConfig {

	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}
}
