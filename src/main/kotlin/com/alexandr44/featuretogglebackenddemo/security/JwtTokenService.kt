package com.alexandr44.featuretogglebackenddemo.security

import com.alexandr44.featuretogglebackenddemo.dto.Token
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtTokenService(
    @Value("\${jwt.secret}") private val base64Secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {

    companion object {
        private val SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256
    }

    private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret))

    fun generateToken(userDetails: UserDetails): Token {
        val now = Date()
        val exp = Date(now.time + expirationMs)
        val token = Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SIGNATURE_ALGORITHM)
            .compact()
        return Token(token, exp)
    }

    fun extractUsername(token: String): String =
        getClaims(token).subject

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val claims = getClaims(token)
        val username = claims.subject
        val notExpired = claims.expiration.after(Date())
        return username == userDetails.username && notExpired
    }

    private fun getClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
}
