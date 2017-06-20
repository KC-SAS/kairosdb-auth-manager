package org.kairosdb.security.auth;

import org.kairosdb.security.auth.core.exception.UnauthorizedClientResponse;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationFilter
{
    boolean tryAuthentication(HttpServletRequest httpServletRequest) throws UnauthorizedClientResponse;
}
