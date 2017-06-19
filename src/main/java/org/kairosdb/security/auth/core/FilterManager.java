package org.kairosdb.security.auth.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.kairosdb.security.auth.core.ModuleTools.newInstance;


public class FilterManager
{
    private static final Logger logger = LoggerFactory.getLogger(FilterManager.class);
    private MethodManager methodManager = new MethodManager();


    public FilterBuilder filter(String path)
    {
        return new FilterBuilder(path, this);
    }

    void addFilter(String method, String path, Class<? extends AuthenticationFilter> filter)
    {
        methodManager.addFilter(method.toUpperCase().trim(), path.trim(), filter);
    }

    Set<AuthenticationFilter> filtersFrom(String method, String path)
    {
        return methodManager.filtersFrom(method.trim().toUpperCase(), pathSplitter(path.trim()));
    }

    List<String> pathSplitter(String path)
    {
        final Set<String> paths = new HashSet<>();
        final String[] parts = path.split("/");
        final StringBuilder formattedPath = new StringBuilder("/");

        if (parts.length < 2)
            paths.add("/");
        paths.add("/*");

        for (int i = 1; i < parts.length; i++)
        {
            formattedPath.append(parts[i]);
            if (i >= parts.length - 1 && !path.endsWith("/"))
            {
                paths.add(formattedPath.toString());
                continue;
            }

            formattedPath.append("/");
            paths.add(formattedPath.toString() + "*");
        }

        return new ArrayList<>(paths);
    }


    //region FilterBuilder
    public static class FilterBuilder
    {
        private final FilterManager manager;
        private final String path;
        private final List<String> methods = new ArrayList<>();

        FilterBuilder(String path, FilterManager manager)
        {
            this.manager = manager;
            this.path = path;
        }

        public FilterBuilder from(String method)
        {
            methods.add(method);
            return this;
        }

        public FilterBuilder through(Class<? extends AuthenticationFilter> filter)
        {
            for (String method : methods)
                manager.addFilter(method, path, filter);
            return this;
        }
    }
    //endregion

    //region MethodManager
    static class MethodManager
    {
        Map<String, PathManager> methodFilters = new HashMap<>();

        void addFilter(String method, String path, Class<? extends AuthenticationFilter> filter)
        {
            if (!methodFilters.containsKey(method))
                methodFilters.put(method, new PathManager());
            methodFilters.get(method).addFilter(path, filter);
        }

        Set<AuthenticationFilter> filtersFrom(String method, List<String> paths)
        {
            if (methodFilters.containsKey(method))
                return methodFilters.get(method).filtersFrom(paths);
            return new HashSet<>();
        }
    }
    //endregion

    //region PathManager
    static class PathManager
    {
        Multimap<String, String> filters = ArrayListMultimap.create();
        Map<String, AuthenticationFilter> instances = new HashMap<>();

        void addFilter(String path, Class<? extends AuthenticationFilter> filter)
        {
            final String canonicalName = filter.getCanonicalName();
            final AuthenticationFilter instance;

            if (!instances.containsKey(canonicalName))
            {
                try
                {
                    instance = newInstance(filter);
                    instances.put(canonicalName, instance);
                } catch (InstantiationException | IllegalAccessException e)
                {
                    logger.error(String.format("Failed to instantiate '%s': %s", filter.getName(), e.getMessage()));
                    return;
                }
            }
            filters.put(path, canonicalName);
        }

        Set<AuthenticationFilter> filtersFrom(List<String> paths)
        {
            Set<AuthenticationFilter> contextFilter = new HashSet<>();

            for (String path : paths)
            {
                if (!filters.containsKey(path))
                    continue;

                for (String instanceKey : filters.get(path))
                    contextFilter.add(instances.get(instanceKey));
            }

            return contextFilter;
        }
    }
    //endregion
}
