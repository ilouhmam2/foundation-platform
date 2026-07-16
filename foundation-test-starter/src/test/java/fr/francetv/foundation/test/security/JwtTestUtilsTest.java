package fr.francetv.foundation.test.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTestUtilsTest {

    private JwtTestUtils jwtTestUtils;

    @BeforeEach
    void setUp() {
        jwtTestUtils = new JwtTestUtils();
    }

    @Test
    void shouldGenerateValidJwtToken() throws Exception {
        String token = jwtTestUtils.generateToken("test-user");

        assertThat(token).isNotBlank();
        SignedJWT jwt = SignedJWT.parse(token);
        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        assertThat(claims.getSubject()).isEqualTo("test-user");
        assertThat(claims.getIssuer()).isEqualTo("http://test-issuer");
        assertThat(claims.getExpirationTime()).isNotNull().isAfter(claims.getIssueTime());
    }

    @Test
    void shouldCreateTokenWithCustomClaims() throws Exception {
        Map<String, Object> extraClaims = Map.of(
                "scope", "read:api",
                "roles", List.of("ADMIN", "USER")
        );

        String token = jwtTestUtils.generateToken("service-account", extraClaims);

        SignedJWT jwt = SignedJWT.parse(token);
        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        assertThat(claims.getSubject()).isEqualTo("service-account");
        assertThat(claims.getStringClaim("scope")).isEqualTo("read:api");
        assertThat(claims.getListClaim("roles")).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void shouldExposePublicRsaKeyForJwtDecoder() {
        assertThat(jwtTestUtils.getPublicRsaKey()).isNotNull();
        assertThat(jwtTestUtils.getPublicRsaKey().isPrivate()).isFalse();
        assertThat(jwtTestUtils.getRsaKey().isPrivate()).isTrue();
    }

    @Test
    void shouldUseConsistentKeyIdInTokenHeader() throws Exception {
        String token = jwtTestUtils.generateToken("user");

        SignedJWT jwt = SignedJWT.parse(token);
        assertThat(jwt.getHeader().getKeyID()).isEqualTo(jwtTestUtils.getRsaKey().getKeyID());
    }
}
