package org.kairosdb.security.auth.core;

import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.test.security.auth.AuthenticationFilterTest;
import org.kairosdb.test.security.auth.AuthenticationModuleTest;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import static org.kairosdb.security.auth.core.utils.AuthTools.getAuthProperties;

public class AuthenticationManagerFilterTest
{
    @Test
    public void ConfigurationTest() throws ServletException
    {
        final AuthenticationModuleTest authenticationModuleTest = new AuthenticationModuleTest();
        final Set<AuthenticationModule> modules = new HashSet<>();
        modules.add(authenticationModuleTest);
        final AuthenticationManagerFilter authenticationManagerFilter = new AuthenticationManagerFilter(null, null, modules);

        Assert.assertFalse(authenticationModuleTest.isConfigured);
        authenticationManagerFilter.init(null);
        Assert.assertTrue(authenticationModuleTest.isConfigured);

        authenticationManagerFilter.destroy();
    }

    @Test
    public void FilterTest() throws ServletException, IOException
    {
        final FilterManager filterManager = new FilterManager();
        final FilterChainTest filterChain = new FilterChainTest();
        final Set<AuthenticationModule> modules = new HashSet<>();
        modules.add(new AuthenticationModuleTest());
        modules.add(new AuthModule());

        final AuthenticationManagerFilter authenticationManagerFilter = new AuthenticationManagerFilter(getAuthProperties(), filterManager, modules);
        authenticationManagerFilter.init(null);


        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("GET", "/test/test"), null, filterChain);
        Assert.assertTrue(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("POST", "/test/test"), null, filterChain);
        Assert.assertTrue(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("GET", "/test/none"), null, filterChain);
        Assert.assertTrue(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("PATCH", "/test/none"), null, filterChain);
        Assert.assertTrue(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("GET", "/test/true"), null, filterChain);
        Assert.assertTrue(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("POST", "/test/false"), null, filterChain);
        Assert.assertTrue(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("POST", "/test/none"), null, filterChain);
        Assert.assertFalse(filterChain.isAuthenticated);



        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("OPTION", "/test/test"), null, filterChain);
        Assert.assertFalse(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("POST", "/test/true"), null, filterChain);
        Assert.assertFalse(filterChain.isAuthenticated);

        filterChain.isAuthenticated = false;
        authenticationManagerFilter.doFilter(new ServletRequestTest("GET", "/"), null, filterChain);
        Assert.assertFalse(filterChain.isAuthenticated);
    }


    static class AuthModule implements AuthenticationModule
    {

        @Override
        public void configure(Properties properties, FilterManager manager)
        {
            manager.filter("/test/true").from("Get").through(AuthenticationFilterTest.class);
            manager.filter("/test/false").from("Post").through(AuthenticationFilterTest.class);
        }
    }

    static class FilterChainTest implements javax.servlet.FilterChain
    {
        boolean isAuthenticated = false;

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException
        {
            isAuthenticated = true;
        }
    }

    static class ServletRequestTest implements HttpServletRequest
    {
        private final String method;
        private final String path;

        ServletRequestTest(String method, String path)
        {
            this.method = method;
            this.path = path;
        }

        @Override
        public String getMethod()
        {
            return method;
        }

        @Override
        public String getContextPath()
        {
            return path;
        }

        //region Useless implementations
        @Override
        public String getAuthType()
        {
            return null;
        }

        @Override
        public Cookie[] getCookies()
        {
            return new Cookie[0];
        }

        @Override
        public long getDateHeader(String s)
        {
            return 0;
        }

        @Override
        public String getHeader(String s)
        {
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(String s)
        {
            return null;
        }

        @Override
        public Enumeration<String> getHeaderNames()
        {
            return null;
        }

        @Override
        public int getIntHeader(String s)
        {
            return 0;
        }

        @Override
        public String getPathInfo()
        {
            return null;
        }

        @Override
        public String getPathTranslated()
        {
            return null;
        }

        @Override
        public String getQueryString()
        {
            return null;
        }

        @Override
        public String getRemoteUser()
        {
            return null;
        }

        @Override
        public boolean isUserInRole(String s)
        {
            return false;
        }

        @Override
        public Principal getUserPrincipal()
        {
            return null;
        }

        @Override
        public String getRequestedSessionId()
        {
            return null;
        }

        @Override
        public String getRequestURI()
        {
            return null;
        }

        @Override
        public StringBuffer getRequestURL()
        {
            return null;
        }

        @Override
        public String getServletPath()
        {
            return null;
        }

        @Override
        public HttpSession getSession(boolean b)
        {
            return null;
        }

        @Override
        public HttpSession getSession()
        {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid()
        {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie()
        {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL()
        {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl()
        {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException
        {
            return false;
        }

        @Override
        public void login(String s, String s1) throws ServletException
        {

        }

        @Override
        public void logout() throws ServletException
        {

        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException
        {
            return null;
        }

        @Override
        public Part getPart(String s) throws IOException, ServletException
        {
            return null;
        }

        @Override
        public Object getAttribute(String s)
        {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames()
        {
            return null;
        }

        @Override
        public String getCharacterEncoding()
        {
            return null;
        }

        @Override
        public void setCharacterEncoding(String s) throws UnsupportedEncodingException
        {

        }

        @Override
        public int getContentLength()
        {
            return 0;
        }

        @Override
        public String getContentType()
        {
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException
        {
            return null;
        }

        @Override
        public String getParameter(String s)
        {
            return null;
        }

        @Override
        public Enumeration<String> getParameterNames()
        {
            return null;
        }

        @Override
        public String[] getParameterValues(String s)
        {
            return new String[0];
        }

        @Override
        public Map<String, String[]> getParameterMap()
        {
            return null;
        }

        @Override
        public String getProtocol()
        {
            return null;
        }

        @Override
        public String getScheme()
        {
            return null;
        }

        @Override
        public String getServerName()
        {
            return null;
        }

        @Override
        public int getServerPort()
        {
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException
        {
            return null;
        }

        @Override
        public String getRemoteAddr()
        {
            return null;
        }

        @Override
        public String getRemoteHost()
        {
            return null;
        }

        @Override
        public void setAttribute(String s, Object o)
        {

        }

        @Override
        public void removeAttribute(String s)
        {

        }

        @Override
        public Locale getLocale()
        {
            return null;
        }

        @Override
        public Enumeration<Locale> getLocales()
        {
            return null;
        }

        @Override
        public boolean isSecure()
        {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String s)
        {
            return null;
        }

        @Override
        public String getRealPath(String s)
        {
            return null;
        }

        @Override
        public int getRemotePort()
        {
            return 0;
        }

        @Override
        public String getLocalName()
        {
            return null;
        }

        @Override
        public String getLocalAddr()
        {
            return null;
        }

        @Override
        public int getLocalPort()
        {
            return 0;
        }

        @Override
        public ServletContext getServletContext()
        {
            return null;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException
        {
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
        {
            return null;
        }

        @Override
        public boolean isAsyncStarted()
        {
            return false;
        }

        @Override
        public boolean isAsyncSupported()
        {
            return false;
        }

        @Override
        public AsyncContext getAsyncContext()
        {
            return null;
        }

        @Override
        public DispatcherType getDispatcherType()
        {
            return null;
        }
        //endregion
    }

}