package org.kairosdb.security.auth;

import org.kairosdb.security.auth.core.exception.UnauthorizedClientResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Filter interface used by the authentication manager
 */
public interface AuthenticationFilter
{
    /**
     * Try to authenticate client with its request.
     * Return {@code true} if the client is
     * authenticated, else {@code false}. <br>
     * If an error occurs, a response can be generated
     * and sent through {@link UnauthorizedClientResponse}.
     *
     * @param httpServletRequest representing the client request
     * @return {@code true} if the authentication succeed, else {@code false}
     * @throws UnauthorizedClientResponse containing the response if an error occurs
     */
    boolean tryAuthentication(HttpServletRequest httpServletRequest) throws UnauthorizedClientResponse;
}
