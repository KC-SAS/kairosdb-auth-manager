package org.kairosdb.security.auth.core;

import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.LoadingModuleException;
import org.kairosdb.security.auth.core.utils.SimpleInjector;
import org.kairosdb.test.security.auth.AuthenticationFilterImpl;
import org.kairosdb.test.security.auth.AuthenticationModuleImpl;

import java.util.*;
import java.util.function.Consumer;

import static org.kairosdb.security.auth.core.Utils.*;
import static org.kairosdb.security.auth.core.utils.PropertiesReader.getProperties;

public class UtilsTest
{
    private Properties properties = getProperties();

    @Test
    public void SplitterTest()
    {
        final Set<String> path_1 = new HashSet<>(Arrays.asList("/", "/*"));
        final Set<String> path_2 = new HashSet<>(Arrays.asList("/*", "/test/*"));
        final Set<String> path_3 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok"));
        final Set<String> path_4 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok/*"));
        final Set<String> path_5 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok/*", "/test/ok/end"));
        final Set<String> path_6 = new HashSet<>(Arrays.asList("/*", "/test/*", "/test/ok/*", "/test/ok/end/*"));

        List<String> paths;

        paths = pathSplitter("/");
        Assert.assertArrayEquals(path_1.toArray(), paths.toArray());

        paths = pathSplitter("/test/");
        Assert.assertArrayEquals(path_2.toArray(), paths.toArray());

        paths = pathSplitter("/test/ok");
        Assert.assertArrayEquals(path_3.toArray(), paths.toArray());

        paths = pathSplitter("/test/ok/*");
        Assert.assertArrayEquals(path_4.toArray(), paths.toArray());

        paths = pathSplitter("/test/ok/end");
        Assert.assertArrayEquals(path_5.toArray(), paths.toArray());

        paths = pathSplitter("/test/ok/end/");
        Assert.assertArrayEquals(path_6.toArray(), paths.toArray());
    }

    @Test
    public void LoadModuleTest()
            throws ClassNotFoundException, LoadingModuleException
    {
        Assert.assertNotNull(loadModule(AuthenticationFilterImpl.AllowFilter.class.getTypeName(), AuthenticationFilter.class));
    }

    @Test(expected = LoadingModuleException.class)
    public void LoadModule_InvalidClass()
            throws ClassNotFoundException, LoadingModuleException
    {
        loadModule(String.class.getCanonicalName(), AuthenticationFilter.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void LoadModule_InvalidName()
            throws ClassNotFoundException, LoadingModuleException
    {
        loadModule("", AuthenticationFilter.class);
    }

    @Test(expected = ClassNotFoundException.class)
    public void LoadModule_ClassNotFound()
            throws ClassNotFoundException, LoadingModuleException
    {
        loadModule("UnknownClass.none", AuthenticationFilter.class);
    }

    @Test
    public void ModuleFromTest() throws ClassNotFoundException, LoadingModuleException
    {
        Set<Class<? extends AuthenticationModule>> modules = modulesFrom(properties, "kairosdb.security.auth.test.modules.");
        Assert.assertEquals(2, modules.size());
        Assert.assertTrue(modules.contains(AuthenticationModuleImpl.FromProperties.class));
        Assert.assertTrue(modules.contains(AuthenticationModuleImpl.FromCode.class));
    }

    @Test
    public void FilterFromTest()
    {
        Set<Consumer<FilterManager>> filters = filterFrom(properties, "kairosdb.security.auth.test.allowed_path", AuthenticationFilterImpl.AllowFilter.class);
        Assert.assertEquals(3, filters.size());
    }

    @Test
    public void PathToFilter()
    {
        final SimpleInjector injector = new SimpleInjector();
        final FilterManagerTest filterManager = new FilterManagerTest(injector);

        Consumer<FilterManager> filter = pathToFilter("/api/*", AuthenticationFilter.class);
        filter.accept(filterManager);
        Assert.assertTrue(filterManager.methods.contains("GET"));
        Assert.assertTrue(filterManager.paths.contains("/api/*"));
        Assert.assertTrue(filterManager.filters.contains(AuthenticationFilter.class));
        filterManager.reset();

        filter = pathToFilter("/api/v2/filter/*|Option|POST|patch|Other", AuthenticationFilter.class);
        filter.accept(filterManager);
        Assert.assertFalse(filterManager.methods.contains("GET"));
        Assert.assertTrue(filterManager.methods.contains("POST"));
        Assert.assertTrue(filterManager.methods.contains("PATCH"));
        Assert.assertTrue(filterManager.methods.contains("OPTION"));
        Assert.assertTrue(filterManager.methods.contains("OTHER"));
        Assert.assertTrue(filterManager.paths.contains("/api/v2/filter/*"));
        Assert.assertTrue(filterManager.filters.contains(AuthenticationFilter.class));
        filterManager.reset();

        filter = pathToFilter("", AuthenticationFilter.class);
        filter.accept(filterManager);
        Assert.assertFalse(filterManager.methods.contains("GET"));
        Assert.assertFalse(filterManager.methods.contains("POST"));
        Assert.assertFalse(filterManager.paths.contains("/api/v2/filter/*"));
        Assert.assertFalse(filterManager.filters.contains(AuthenticationFilter.class));
    }

    //region Tests classes

    private static class FilterManagerTest extends FilterManager
    {
        Set<String> methods = new HashSet<>();
        Set<String> paths = new HashSet<>();
        Set<Class<? extends AuthenticationFilter>> filters = new HashSet<>();

        FilterManagerTest(Injector injector)
        {
            super(injector);
        }

        @Override
        void addFilter(String method, String path, Class<? extends AuthenticationFilter> filter)
        {
            methods.add(method.toUpperCase());
            paths.add(path);
            filters.add(filter);
        }

        void reset()
        {
            methods.clear();
            paths.clear();
            filters.clear();
        }
    }
    //endregion
}