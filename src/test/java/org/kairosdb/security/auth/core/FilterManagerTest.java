package org.kairosdb.security.auth.core;

import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.core.utils.SimpleInjector;
import org.kairosdb.test.security.auth.AuthenticationFilterImpl;

import java.util.Set;

public class FilterManagerTest
{
    @Test
    public void ValidFilterTest() throws Exception
    {
        final SimpleInjector injector = new SimpleInjector();
        final FilterManager filterManager = new FilterManager(injector);
        Set<AuthenticationFilter> filters;

        filterManager.filter("/").from("GET").through(AuthenticationFilterImpl.AllowFilter.class);
        injector.throwIfFailure();

        filters = filterManager.filtersFrom("get", "/");
        Assert.assertEquals(1, filters.size());
        filters = filterManager.filtersFrom("get", "/none");
        Assert.assertEquals(0, filters.size());
        filters = filterManager.filtersFrom("patch", "/");
        Assert.assertEquals(0, filters.size());

        filterManager.filter("/*").from("POST").through(AuthenticationFilterImpl.AllowFilter.class);
        injector.throwIfFailure();

        filters = filterManager.filtersFrom("get", "/test");
        Assert.assertEquals(0, filters.size());
        filters = filterManager.filtersFrom("post", "/test");
        Assert.assertEquals(1, filters.size());

        filterManager.filter("/test/node/*").from("PATCH").from("POST").through(AuthenticationFilterImpl.DenyFilter.class);
        injector.throwIfFailure();

        filters = filterManager.filtersFrom("get", "/test/node/test");
        Assert.assertEquals(0, filters.size());
        filters = filterManager.filtersFrom("patch", "/test/node/test");
        Assert.assertEquals(1, filters.size());
        filters = filterManager.filtersFrom("post", "/test/node/test");
        Assert.assertEquals(2, filters.size());
    }
}