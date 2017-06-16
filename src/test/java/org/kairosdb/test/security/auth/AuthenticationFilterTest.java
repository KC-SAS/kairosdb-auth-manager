package org.kairosdb.test.security.auth;

import org.kairosdb.security.auth.AuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilterTest implements AuthenticationFilter
{

    @Override
    public boolean tryAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        return true;
    }
}