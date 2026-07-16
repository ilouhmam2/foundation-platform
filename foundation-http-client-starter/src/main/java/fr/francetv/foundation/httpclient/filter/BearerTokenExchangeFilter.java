package fr.francetv.foundation.httpclient.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * {@link ExchangeFilterFunction} that injects a Bearer token from the current
 * Spring Security context into outgoing HTTP requests.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>Reactive context ({@link ReactiveSecurityContextHolder}) — for WebFlux applications</li>
 *   <li>Thread-local context ({@link SecurityContextHolder}) — for servlet applications using
 *       {@code WebClient} for outbound calls</li>
 * </ol>
 *
 * <p>Supported authentication types:
 * <ul>
 *   <li>Any authentication whose {@code credentials} is a non-blank {@link String}</li>
 *   <li>{@link AbstractOAuth2Token} subtypes (e.g. {@code Jwt}, {@code OAuth2AccessToken}) — value
 *       retrieved via {@link AbstractOAuth2Token#getTokenValue()}</li>
 * </ul>
 *
 * <p>If the {@code Authorization} header is already present on the request, it is not overwritten.
 * If no authenticated principal is found, the request is forwarded unchanged.
 */
public class BearerTokenExchangeFilter implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (request.headers().getFirst(HttpHeaders.AUTHORIZATION) != null) {
            return next.exchange(request);
        }
        return resolveToken()
                .map(token -> ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build())
                .defaultIfEmpty(request)
                .flatMap(next::exchange);
    }

    private Mono<String> resolveToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> extractToken(ctx.getAuthentication()))
                .filter(Objects::nonNull)
                .switchIfEmpty(
                        Mono.fromSupplier(() -> extractToken(
                                SecurityContextHolder.getContext().getAuthentication()))
                             .filter(Objects::nonNull)
                );
    }

    private static String extractToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // Only extract tokens from OAuth2 authentication types.
        // Deliberately skips String credentials to avoid accidentally propagating
        // passwords from UsernamePasswordAuthenticationToken.
        if (authentication.getCredentials() instanceof AbstractOAuth2Token oauth2Token) {
            String value = oauth2Token.getTokenValue();
            return (value != null && !value.isBlank()) ? value : null;
        }
        return null;
    }
}
