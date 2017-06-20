package org.kairosdb.security.auth.core.container;


import com.google.inject.ConfigurationException;
import com.google.inject.ProvisionException;
import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.core.functionnal.Instantiator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.kairosdb.security.auth.core.Utils.pathSplitter;

public class FilterContainer
{
    private final Map<String, AuthenticationFilter> filterInstances = new HashMap<>();
    private final MethodContainer methodContainer = new MethodContainer();
    private final Instantiator instantiator;

    public FilterContainer(Instantiator instantiator)
    {
        this.instantiator = instantiator;
    }

    public void addFilter(String method, String path, Class<? extends AuthenticationFilter> filter)
            throws ConfigurationException, ProvisionException
    {
        String canonicalName = filter.getCanonicalName();
        if (!filterInstances.containsKey(canonicalName))
            filterInstances.put(canonicalName, instantiator.apply(filter));
        methodContainer.addFilter(method.toUpperCase().trim(), path.trim(), canonicalName);
    }

    public Set<AuthenticationFilter> filtersFrom(String method, String path)
    {
        final Set<AuthenticationFilter> filters = new HashSet<>();

        for (String filter : methodContainer.filtersFrom(method.trim().toUpperCase(), pathSplitter(path.trim())))
            filters.add(filterInstances.get(filter));
        return filters;
    }
}
