package org.kairosdb.security.auth.core.utils.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class FilterChainTest implements javax.servlet.FilterChain
{
    public boolean isAuthenticated = false;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException
    {
        isAuthenticated = true;
    }
}