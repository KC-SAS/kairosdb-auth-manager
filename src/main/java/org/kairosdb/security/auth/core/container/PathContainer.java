package org.kairosdb.security.auth.core.container;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PathContainer
{
    private Multimap<String, String> filtersByPath = HashMultimap.create();

    void addFilter(String path, String filter)
    {
        filtersByPath.put(path, filter);
    }

    Set<String> filtersFrom(List<String> paths)
    {
        Set<String> contextFilter = new HashSet<>();

        for (String path : paths)
            if (filtersByPath.containsKey(path))
                contextFilter.addAll(filtersByPath.get(path));

        return contextFilter;
    }
}
