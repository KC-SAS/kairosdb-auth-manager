package org.kairosdb.security.auth.core;

import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.LoadingModuleException;
import org.kairosdb.test.security.auth.AuthenticationFilterTest;
import org.kairosdb.test.security.auth.AuthenticationModuleTest;

import java.util.*;
import java.util.function.Consumer;

import static org.kairosdb.security.auth.core.ModuleTools.*;
import static org.kairosdb.security.auth.core.utils.AuthTestTools.getAuthProperties;

public class ModuleToolsTest
{
    private Properties properties = getAuthProperties();

    @Test
    public void LoadModuleTest()
            throws ClassNotFoundException, LoadingModuleException
    {
        Assert.assertNotNull(loadModule(AuthenticationFilterTest.class.getCanonicalName(), AuthenticationFilter.class));
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
    public void NewInstanceTest()
            throws IllegalAccessException, InstantiationException
    {
        String s = newInstance(String.class);
        Assert.assertEquals(String.class, s.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void NewInstance_IllegalArgument()
            throws IllegalAccessException, InstantiationException
    {
        newInstance(null);
    }

    @Test
    public void NewInstance_InnerClass() throws IllegalAccessException
    {
        try
        {
            newInstance(InnerTestClass.class);
            Assert.fail();
        } catch (InstantiationException e)
        {
            final String format = "'%s' is an inner class but must be static to be instantiated.";
            Assert.assertEquals(String.format(format, InnerTestClass.class.getName()), e.getMessage());
        }
    }

    @Test
    public void NewInstance_ParameterlessCtor() throws IllegalAccessException
    {
        try
        {
            newInstance(ParameterlessCtorTestClass.class);
            Assert.fail();
        } catch (InstantiationException e)
        {
            final String format = "'%s' must implement a parameterless ctor.";
            Assert.assertEquals(String.format(format, ParameterlessCtorTestClass.class.getName()), e.getMessage());
        }
    }

    @Test
    public void ModuleFromTest() throws ClassNotFoundException, LoadingModuleException
    {
        Set<Class<? extends AuthenticationModule>> modules = modulesFrom(properties, "kairosdb.security.auth.test.modules.");
        Assert.assertEquals(2, modules.size());
        Assert.assertTrue(modules.contains(AuthenticationModuleTest.class));
        Assert.assertTrue(modules.contains(ModuleTestClass.class));
    }

    @Test(expected = ClassNotFoundException.class)
    public void ModuleFromTest_LoadingModuleException() throws ClassNotFoundException, LoadingModuleException
    {
        Set<Class<? extends AuthenticationModule>> modules = modulesFrom(properties, "kairosdb.security.auth.test.invalid.modules.");
    }

    @Test
    public void FilterFromTest()
    {
        Set<Consumer<FilterManager>> filters = filterFrom(properties, "kairosdb.security.auth.test.path.", AuthenticationFilterTest.class);
        Assert.assertEquals(3, filters.size());
    }

    @Test
    public void PathToFilter()
    {
        final FilterManagerTest filterManager = new FilterManagerTest();

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
    private class InnerTestClass {}

    private static class ParameterlessCtorTestClass
    {
        ParameterlessCtorTestClass(String s) {}
    }

    private static class ModuleTestClass implements AuthenticationModule
    {
        @Override
        public void configure(Properties properties, FilterManager manager)
        {

        }
    }

    private static class FilterManagerTest extends FilterManager
    {
        Set<String> methods = new HashSet<>();
        Set<String> paths = new HashSet<>();
        Set<Class<? extends AuthenticationFilter>> filters = new HashSet<>();

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