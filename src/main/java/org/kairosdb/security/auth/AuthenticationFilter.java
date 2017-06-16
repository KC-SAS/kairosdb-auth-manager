package org.kairosdb.security.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthenticationFilter
{
    boolean tryAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
