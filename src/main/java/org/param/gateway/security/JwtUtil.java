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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

/**
 * Author: PARAMESHWARAN PV
 * Date: 27-Jun-2025 : 8:13:40 AM
 * Since: 1.0.0
 * @See #
 */

@Component
public class JwtUtil {

	private PublicKey publicKey;

	@PostConstruct
	public void init() {
		try {
			// Load the private key file from resources
			ClassPathResource resource = new ClassPathResource("public_key.pem");
			InputStream inputStream = resource.getInputStream();
			String publicKeyPem = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

			String keyContent = publicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "").replaceAll("\\s+", "");

			byte[] decoded = Base64.getDecoder().decode(keyContent);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			this.publicKey = keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load RSA public key", e);
		}
	}

	public Claims validateTokenAndGetClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
	}

	public boolean isTokenValid(String token) {
		try {
			Claims claims = validateTokenAndGetClaims(token);
			return claims.getExpiration().after(new Date());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
