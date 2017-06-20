package org.kairosdb.security.auth.core;

import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.utils.SimpleInjector;
import org.kairosdb.security.auth.core.utils.servlet.FilterChainTest;
import org.kairosdb.security.auth.core.utils.servlet.HttpRequestTest;
import org.kairosdb.security.auth.core.utils.servlet.HttpResponseTest;
import org.kairosdb.test.security.auth.AuthenticationModuleImpl;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.kairosdb.security.auth.core.AuthenticationManagerFilterTest.Authorization.ALLOWED;
import static org.kairosdb.security.auth.core.AuthenticationManagerFilterTest.Authorization.DENIED;
import static org.kairosdb.security.auth.core.utils.PropertiesReader.getProperties;

public class AuthenticationManagerFilterTest
{
    @Test
    public void ConfigurationTest() throws ServletException
    {
        final AuthenticationModuleImpl.FromProperties authenticationModule = new AuthenticationModuleImpl.FromProperties();
        final Set<AuthenticationModule> modules = new HashSet<>();
        modules.add(authenticationModule);

        Assert.assertFalse(authenticationModule.isConfigured);
        final AuthenticationManagerFilter authenticationManagerFilter = new AuthenticationManagerFilter(null, null, modules);
        Assert.assertTrue(authenticationModule.isConfigured);

        authenticationManagerFilter.destroy();
    }

    @Test
    public void FilterTest() throws Exception
    {
        final SimpleInjector injector = new SimpleInjector();
        final FilterManager filterManager = new FilterManager(injector);
        final HttpServletResponse httpResponse = new HttpResponseTest();
        final FilterChainTest filterChain = new FilterChainTest();
        final Set<AuthenticationModule> modules = new HashSet<>();
        modules.add(new AuthenticationModuleImpl.FromProperties());
        modules.add(new AuthenticationModuleImpl.FromCode());


        final AuthenticationManagerFilter managerFilter = new AuthenticationManagerFilter(getProperties(), filterManager, modules);
        managerFilter.init(null);
        injector.throwIfFailure();

        List<FilterTest> filterTestSuite = new ArrayList<>(Arrays.asList(
                new FilterTest("GET", "/test/test", DENIED),         // /test/test = /test/* + GET -> denied (DenyFilter)
                new FilterTest("POST", "/test/test", ALLOWED),       // /test/test = /test/* + POST -> allowed (no link with POST)
                new FilterTest("PATCH", "/test/api/entry", ALLOWED), // /test/api/entry = /test/api/* + PATCH -> allowed (AllowFilter)
                new FilterTest("GET", "/test/api/entry", ALLOWED),   // /test/api/entry = /test/api/* + GET -> allowed (AllowFilter)
                new FilterTest("POST", "/test/api/entry", ALLOWED),   // /test/api/entry = /test/api/* + POST -> allowed (AllowFilter)
                new FilterTest("GET", "/test/none", ALLOWED),        // /test/none + GET -> allowed (AllowFilter)
                new FilterTest("PATCH", "/test/none", ALLOWED),      // /test/none + PATCH -> allowed (no link with PATCH)
                new FilterTest("POST", "/test/none", DENIED),        // /test/none + POST -> denied (DenyFilter)
                new FilterTest("GET", "/test/allow", ALLOWED),       // /test/allow + GET -> allowed (no link with GET)
                new FilterTest("PATCH", "/test/allow", ALLOWED),     // /test/allow + PATCH -> allowed (AllowFilter)
                new FilterTest("POST", "/test/allow", DENIED),       // /test/allow + POST -> denied (DenyFilter)
                new FilterTest("POST", "/test/deny", DENIED)         // /test/deny + POST -> denied (DenyFilter)
        ));

        for (FilterTest filterTest : filterTestSuite)
            filterTest.execute(filterChain, managerFilter, httpResponse);
    }

    enum Authorization
    {
        ALLOWED(true),
        DENIED(false);

        final boolean authorized;

        Authorization(boolean authorized) {this.authorized = authorized;}

        boolean isAuthorized() {return authorized;}
    }

    static class FilterTest
    {
        final String method;
        final String path;
        final Authorization expected;

        FilterTest(String method, String path, Authorization expected)
        {
            this.method = method;
            this.path = path;
            this.expected = expected;
        }

        void execute(FilterChainTest filterChain, AuthenticationManagerFilter managerFilter, ServletResponse httpResponse)
                throws IOException, ServletException
        {
            final String message = String.format("[%s] '%s' test failure. Must be %s, but not currently.", method, path, expected.toString());

            filterChain.isAuthenticated = false;
            managerFilter.doFilter(new HttpRequestTest(method, path), httpResponse, filterChain);
            Assert.assertEquals(message, expected.isAuthorized(), filterChain.isAuthenticated);
        }
    }
}