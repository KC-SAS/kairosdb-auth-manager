package org.kairosdb.security.auth.core.container;


import java.util.*;

class MethodContainer
{
    private Map<String, PathContainer> filtersByMethod = new HashMap<>();

    void addFilter(String method, String path, String filter)
    {
        if (!filtersByMethod.containsKey(method))
            filtersByMethod.put(method, new PathContainer());
        filtersByMethod.get(method).addFilter(path, filter);
    }

    Set<String> filtersFrom(String method, List<String> paths)
    {
        if (filtersByMethod.containsKey(method))
            return filtersByMethod.get(method).filtersFrom(paths);
        return new HashSet<>();
    }
}
