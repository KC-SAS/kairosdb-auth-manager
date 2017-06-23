package org.kairosdb.security.auth.core;

import com.google.inject.Inject;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.UnauthorizedClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class AuthenticationManagerFilter implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerFilter.class);
    private final UnauthorizedClientResponse unauthorizedResponse;
    private final FilterManager filterManager;

    @Inject
    public AuthenticationManagerFilter(Properties properties, FilterManager filterManager, Set<AuthenticationModule> modules)
    {
        this.filterManager = filterManager;
        unauthorizedResponse = new UnauthorizedClientResponse(Integer.MIN_VALUE, response -> response.sendError(401));

        for (AuthenticationModule module : modules)
            module.configure(properties, filterManager);
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        final String method = httpServletRequest.getMethod();
        final String requestPath = httpServletRequest.getRequestURI();
        final Set<AuthenticationFilter> filters = filterManager.filtersFrom(method, requestPath);
        final Optional<Set<UnauthorizedClientResponse>> responses;

        logger.debug(String.format("Authentication filter '%s' (%s)", requestPath, method));
        logger.debug(String.format("%d filter(s) found", filters.size()));

        responses = tryAuthentication(filters, httpServletRequest);

        if (responses.isPresent())
        {
            responses.get().stream()
                    .sorted((lhs, rhs) -> rhs.weight() - lhs.weight())
                    .findFirst().orElse(unauthorizedResponse)
                    .sendResponse(httpServletResponse);
        }
        else
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy()
    {
    }


    private Optional<Set<UnauthorizedClientResponse>> tryAuthentication(Set<AuthenticationFilter> filters, HttpServletRequest httpRequest)
    {
        final Set<UnauthorizedClientResponse> responses = new HashSet<>();

        for (AuthenticationFilter filter : filters)
        {
            try
            {
                if (filter.tryAuthentication(httpRequest))
                    return Optional.empty();
                responses.add(unauthorizedResponse);

            } catch (UnauthorizedClientResponse response)
            {
                responses.add(response);
            }
        }

        if (responses.isEmpty())
            return Optional.empty();
        return Optional.of(responses);
    }
}
