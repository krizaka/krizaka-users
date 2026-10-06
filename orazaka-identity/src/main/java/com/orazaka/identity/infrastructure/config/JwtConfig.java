package com.orazaka.identity.infrastructure.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * HS256 encoder/decoder pair over the shared identity secret. The encoder lives with the token
 * issuer (identity); the decoder is used by every host that validates sessions locally (router
 * during the dual-run, edge/services after the cutover) — no per-request identity hop.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
class JwtConfig {

  private static SecretKeySpec signingKey(JwtProperties properties) {
    return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

  @Bean
  JwtEncoder identityJwtEncoder(JwtProperties properties) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey(properties)));
  }

  @Bean
  JwtDecoder identityJwtDecoder(JwtProperties properties) {
    return NimbusJwtDecoder.withSecretKey(signingKey(properties))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }
}
