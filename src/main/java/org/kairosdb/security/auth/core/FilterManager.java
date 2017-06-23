package org.kairosdb.security.auth.core;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.core.container.FilterContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class to easily manage several filters
 */
public class FilterManager
{
    private static final Logger logger = LoggerFactory.getLogger(FilterManager.class);
    private final FilterContainer filterContainer;

    @Inject
    FilterManager(Injector injector)
    {
        filterContainer = new FilterContainer(injector::getInstance);
    }

    /**
     * Create a new filter rule.
     *
     * @param path Web path where the filter is applied
     * @return {@link FilterBuilder} used to build the filter rule
     */
    public FilterBuilder filter(String path)
    {
        return new FilterBuilder(path, this);
    }

    //region Internal methods
    void addFilter(String method, String path, Class<? extends AuthenticationFilter> filter)
    {
        try
        {
            filterContainer.addFilter(method, path, filter);
        } catch (Exception e)
        {
            logger.error(String.format("'%s' must be able to be instanced by Guice", filter.getName()), e);
        }
    }

    Set<AuthenticationFilter> filtersFrom(String method, String path)
    {
        return filterContainer.filtersFrom(method, path);
    }
    //endregion

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

        /**
         * Add a method to the filter rule.
         *
         * @param method HTTP method required to use the filter
         * @return {@link FilterBuilder} used to build the filter rule
         */
        public FilterBuilder from(String method)
        {
            methods.add(method);
            return this;
        }

        /**
         * Add an {@link AuthenticationFilter}, applied by the filter rule.
         *
         * @param filter {@link AuthenticationFilter} used to authentication the client
         * @return {@link FilterBuilder} used to build the filter rule
         */
        public FilterBuilder through(Class<? extends AuthenticationFilter> filter)
        {
            for (String method : methods)
                manager.addFilter(method, path, filter);
            return this;
        }
    }
}
