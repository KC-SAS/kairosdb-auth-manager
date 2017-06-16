package org.kairosdb.security.auth.core;

import com.google.inject.Inject;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class AuthenticationManagerFilter implements Filter
{
    private final Properties properties;
    private final FilterManager filterManager;
    private final Set<AuthenticationModule> modules;

    @Inject
    public AuthenticationManagerFilter(Properties properties, FilterManager filterManager, Set<AuthenticationModule> modules)
    {
        this.properties = properties;
        this.filterManager = filterManager;
        this.modules = modules;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        for (AuthenticationModule module : modules)
            module.configure(properties, filterManager);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        final String method = httpServletRequest.getMethod();
        final String contextPath = httpServletRequest.getContextPath();
        final Set<AuthenticationFilter> filters = filterManager.filtersFrom(method, contextPath);

        for (AuthenticationFilter filter : filters)
        {
            if (filter.tryAuthentication(httpServletRequest, httpServletResponse))
            {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
    }

    @Override
    public void destroy() { }
}
