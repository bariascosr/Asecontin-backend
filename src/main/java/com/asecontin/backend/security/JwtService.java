package com.asecontin.backend.security;

import com.asecontin.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	private final JwtProperties properties;
	private final SecretKey key;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String createToken(String email, String rol) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + properties.expirationMs());
		return Jwts.builder()
				.subject(email)
				.claim("rol", rol)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	public Claims validateAndGetClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String getEmailFromClaims(Claims claims) {
		return claims.getSubject();
	}
}
