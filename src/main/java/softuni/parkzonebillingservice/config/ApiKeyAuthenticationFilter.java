package softuni.parkzonebillingservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import softuni.parkzonebillingservice.exception.InvalidApiKeyException;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String X_API_KEY = "X-API-Key";

    private final String validApiKey;

    public ApiKeyAuthenticationFilter(@Value("${billing.service.api.key}") String validApiKey) {
        this.validApiKey = validApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String apiKey = request.getHeader(X_API_KEY);

            if (apiKey == null || apiKey.isBlank()) {
                throw new InvalidApiKeyException("Missing API Key header!", HttpStatus.UNAUTHORIZED);
            }

            if (!apiKey.equals(validApiKey)) {
                throw new InvalidApiKeyException("Invalid API Key!", HttpStatus.FORBIDDEN);
            }

            Authentication authentication = new ApiKeyAuthentication(apiKey);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (InvalidApiKeyException e) {
            response.setStatus(e.getHttpStatus().value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "status": %d,
                      "error": "%s",
                      "message": "%s"
                    }
                    """.formatted(
                    e.getHttpStatus().value(),
                    e.getHttpStatus().getReasonPhrase(),
                    e.getMessage()
            ));
        }
    }
}
