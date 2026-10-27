package com.krizaka.users.infrastructure.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Encoder/decoder of the identity tokens (ADR-001). Session tokens are ES256, signed by this issuer
 * only and published at {@code /.well-known/jwks.json}; the decoder verifies them against the
 * issuer's own keys by {@code kid}. HS256 stays signed and verified when no issuer key is
 * configured, and verifies the {@code SERVICE} tokens until per-caller keys land (slice 3).
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

  private static SecretKeySpec hmacKey(JwtProperties properties) {
    return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

  @Bean
  public IssuerKeys issuerKeys(JwtProperties properties) {
    return IssuerKeys.from(properties.signing());
  }

  @Bean
  public JwtEncoder identityJwtEncoder(JwtProperties properties, IssuerKeys keys) {
    if (keys.enabled()) {
      return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(keys.signing())));
    }
    return new NimbusJwtEncoder(new ImmutableSecret<>(hmacKey(properties)));
  }

  @Bean
  public JwtDecoder identityJwtDecoder(JwtProperties properties, IssuerKeys keys) {
    JwtDecoder hmac =
        NimbusJwtDecoder.withSecretKey(hmacKey(properties))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    var processor = new DefaultJWTProcessor<SecurityContext>();
    processor.setJWSKeySelector(
        new JWSVerificationKeySelector<>(
            JWSAlgorithm.ES256, new ImmutableJWKSet<SecurityContext>(keys.publicSet())));
    JwtDecoder es256 = new NimbusJwtDecoder(processor);
    return token -> {
      try {
        boolean issuerSigned =
            JWSAlgorithm.ES256.equals(JWTParser.parse(token).getHeader().getAlgorithm());
        return issuerSigned ? es256.decode(token) : hmac.decode(token);
      } catch (ParseException e) {
        throw new BadJwtException("Malformed token");
      }
    };
  }
}
