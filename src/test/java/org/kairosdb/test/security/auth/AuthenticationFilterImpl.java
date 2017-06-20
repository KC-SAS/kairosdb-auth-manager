package org.kairosdb.test.security.auth;

import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.core.exception.UnauthorizedClientResponse;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationFilterImpl
{
    public static class AllowFilter implements AuthenticationFilter
    {
        @Override
        public boolean tryAuthentication(HttpServletRequest httpServletRequest) throws UnauthorizedClientResponse { return true; }
    }

    public static class DenyFilter implements AuthenticationFilter
    {
        @Override
        public boolean tryAuthentication(HttpServletRequest httpServletRequest) throws UnauthorizedClientResponse { return false; }
    }
}