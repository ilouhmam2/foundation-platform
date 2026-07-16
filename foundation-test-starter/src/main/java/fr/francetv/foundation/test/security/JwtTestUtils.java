package fr.francetv.foundation.test.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Test utility for generating signed JWT tokens using an ephemeral RSA key pair.
 *
 * <p>One RSA key pair is generated per instance. When used as a Spring bean (via
 * {@link fr.francetv.foundation.test.autoconfigure.TestAutoConfiguration}), the same key pair
 * is reused for all token generations within a given application context.
 *
 * <p>Usage in a test:
 * <pre>{@code
 * @Autowired
 * JwtTestUtils jwtTestUtils;
 *
 * String token = jwtTestUtils.generateToken("user@example.com", Map.of("scope", "read:api"));
 * }</pre>
 *
 * <p>To configure a {@code JwtDecoder} that accepts tokens produced by this utility, use
 * {@link #getPublicRsaKey()} to build a decoder backed by the matching public key.
 */
public class JwtTestUtils {

    private static final String DEFAULT_ISSUER = "http://test-issuer";
    private static final long TOKEN_VALIDITY_MS = 3_600_000L;

    private final RSAKey rsaKey;

    public JwtTestUtils() {
        try {
            this.rsaKey = new RSAKeyGenerator(2048)
                    .keyID("foundation-test-key")
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to generate RSA key pair for test JWT", e);
        }
    }

    /**
     * Returns the private+public RSA key pair.
     * Use {@link #getPublicRsaKey()} to expose only the public part to a {@code JwtDecoder}.
     */
    public RSAKey getRsaKey() {
        return rsaKey;
    }

    /**
     * Returns the public-only RSA key, suitable for configuring a {@code NimbusJwtDecoder}.
     */
    public RSAKey getPublicRsaKey() {
        return rsaKey.toPublicJWK();
    }

    /**
     * Generates a signed JWT for the given subject with no additional claims.
     *
     * @param subject the JWT subject (e.g. a user ID or username)
     * @return serialized, signed JWT token
     */
    public String generateToken(String subject) {
        return generateToken(subject, Collections.emptyMap());
    }

    /**
     * Generates a signed JWT for the given subject with additional custom claims.
     *
     * @param subject      the JWT subject
     * @param extraClaims  additional claims to include (e.g. {@code scope}, {@code roles})
     * @return serialized, signed JWT token
     */
    public String generateToken(String subject, Map<String, Object> extraClaims) {
        try {
            Date now = new Date();
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(DEFAULT_ISSUER)
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + TOKEN_VALIDITY_MS));

            extraClaims.forEach(claimsBuilder::claim);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
            signedJWT.sign(new RSASSASigner(rsaKey));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign test JWT", e);
        }
    }
}
