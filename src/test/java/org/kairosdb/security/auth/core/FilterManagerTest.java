package org.kairosdb.security.auth.core;

import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.test.security.auth.AuthenticationFilterTest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class FilterManagerTest
{
    @Test
    public void SplitterTest()
    {
        final FilterManager filterManager = new FilterManager();
        final Set<String> path_1 = new HashSet<>(Arrays.asList("/", "/*"));
        final Set<String> path_2 = new HashSet<>(Arrays.asList("/*", "/test/*"));
        final Set<String> path_3 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok"));
        final Set<String> path_4 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok/*"));
        final Set<String> path_5 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok/*", "/test/ok/end"));
        final Set<String> path_6 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok/*", "/test/ok/end/*"));

        List<String> paths;

        paths = filterManager.pathSplitter("/");
        Assert.assertArrayEquals(path_1.toArray(), paths.toArray());

        paths = filterManager.pathSplitter("/test/");
        Assert.assertArrayEquals(path_2.toArray(), paths.toArray());

        paths = filterManager.pathSplitter("/test/ok");
        Assert.assertArrayEquals(path_3.toArray(), paths.toArray());

        paths = filterManager.pathSplitter("/test/ok/*");
        Assert.assertArrayEquals(path_4.toArray(), paths.toArray());

        paths = filterManager.pathSplitter("/test/ok/end");
        Assert.assertArrayEquals(path_5.toArray(), paths.toArray());

        paths = filterManager.pathSplitter("/test/ok/end/");
        Assert.assertArrayEquals(path_6.toArray(), paths.toArray());
    }

    @Test
    public void ValidFilterTest()
    {
        final FilterManager filterManager = new FilterManager();
        Set<AuthenticationFilter> filters;

        filterManager.filter("/").from("gEt").through(AuthenticationFilterTest.class);
        filters = filterManager.filtersFrom("get", "/");
        Assert.assertEquals(1, filters.size());
        filters = filterManager.filtersFrom("get", "/none");
        Assert.assertEquals(0, filters.size());
        filters = filterManager.filtersFrom("patch", "/");
        Assert.assertEquals(0, filters.size());

        filterManager.filter("/*").from("POST").through(AuthenticationFilterTest.class);
        filters = filterManager.filtersFrom("get", "/test");
        Assert.assertEquals(0, filters.size());
        filters = filterManager.filtersFrom("post", "/test");
        Assert.assertEquals(1, filters.size());

        filterManager.filter("/test/node/*").from("PATCH").from("POST").through(SpecialAuthFilter.class);
        filters = filterManager.filtersFrom("get", "/test/node/test");
        Assert.assertEquals(0, filters.size());
        filters = filterManager.filtersFrom("patch", "/test/node/test");
        Assert.assertEquals(1, filters.size());
        filters = filterManager.filtersFrom("post", "/test/node/test");
        Assert.assertEquals(2, filters.size());
    }

    public static class SpecialAuthFilter implements AuthenticationFilter
    {

        @Override
        public boolean tryAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        {
            return false;
        }
    }
}