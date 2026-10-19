package za.ac.cput.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path = request.getServletPath();

        /*
         * Login and registration do not have a JWT yet.
         * Therefore, do not run the JWT filter for authentication
         * endpoints.
         */
        return path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // ---------------------------------------------------------
        // 1. Check for Bearer token
        // ---------------------------------------------------------

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            jwt = authHeader.substring(7).trim();

            if (!jwt.isEmpty()) {

                try {

                    email = jwtService.extractEmail(jwt);

                } catch (Exception exception) {

                    // Invalid, malformed, or expired JWT.
                    email = null;
                }
            }
        }

        // ---------------------------------------------------------
        // 2. Authenticate the request
        // ---------------------------------------------------------

        if (email != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            try {

                /*
                 * Validate the JWT before loading the user.
                 */
                if (jwtService.isTokenValid(jwt, email)) {

                    UserDetails userDetails =
                            userDetailsService
                                    .loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception exception) {

                /*
                 * Invalid token or missing user.
                 * Clear the context and allow Spring Security
                 * to decide whether the endpoint requires
                 * authentication.
                 */
                SecurityContextHolder
                        .clearContext();
            }
        }

        // ---------------------------------------------------------
        // 3. Continue request
        // ---------------------------------------------------------

        filterChain.doFilter(request, response);
    }
}

