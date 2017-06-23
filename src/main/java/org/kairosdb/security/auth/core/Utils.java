package org.kairosdb.security.auth.core;

import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.LoadingModuleException;

import java.util.*;
import java.util.function.Consumer;

public class Utils
{
    //region Filter tools
    public static Set<Consumer<FilterManager>> filtersFrom(Properties properties, String prefix, Class<? extends AuthenticationFilter> filter)
    {
        Set<Consumer<FilterManager>> filters = new HashSet<>();

        for (Object key : properties.keySet())
            if (key.toString().startsWith(prefix))
                filters.add(pathToFilter(properties.getProperty(key.toString()), filter));

        return filters;
    }

    public static Consumer<FilterManager> pathToFilter(String path, Class<? extends AuthenticationFilter> filter)
    {
        String[] items = path.split("\\|");

        if (items.length == 1 && items[0].isEmpty())
            return m ->
            {
            };

        return manager ->
        {
            FilterManager.FilterBuilder builder = manager.filter(items[0].trim());

            if (items.length < 2)
                builder.from("GET");
            for (int i = 1; i < items.length; i++)
                if (!items[i].isEmpty())
                    builder.from(items[i].trim().toUpperCase());

            builder.through(filter);
        };
    }
    //endregion

    //region Modules tools
    public static <T> Class<? extends T> loadModule(String moduleName, Class<T> originClazz)
            throws IllegalArgumentException, LoadingModuleException, ClassNotFoundException
    {
        if (moduleName == null || moduleName.isEmpty())
            throw new IllegalArgumentException("Module name cannot be empty.");

        Class<?> clazz = originClazz.getClassLoader().loadClass(moduleName);
        if (originClazz.isAssignableFrom(clazz))
            return (Class<? extends T>) clazz;

        throw new LoadingModuleException(moduleName, String
                .format("Invalid class, must extend '%s'", originClazz.getName()));
    }

    public static Set<Class<? extends AuthenticationModule>> modulesFrom(Properties properties, String prefix)
            throws IllegalArgumentException, LoadingModuleException, ClassNotFoundException
    {
        Set<Class<? extends AuthenticationModule>> moduleClazz = new HashSet<>();

        for (Object key : properties.keySet())
        {
            if (key.toString().startsWith(prefix))
            {
                final Class<? extends AuthenticationModule> clazz;
                clazz = loadModule(properties.getProperty(key.toString()), AuthenticationModule.class);
                moduleClazz.add(clazz);
            }
        }

        return moduleClazz;
    }
    //endregion

    //region Path tools
    public static List<String> pathSplitter(String path)
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
    //endregion
}
