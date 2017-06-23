package org.kairosdb.security.auth.core;

import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.LoadingModuleException;

import java.util.*;
import java.util.function.Consumer;

public class Utils
{
    /**
     * Tool to generate a collection of {@link Consumer}
     * from path list in {@link Properties}. <br>
     * Theses {@link Consumer} must be used with the {@link FilterManager}
     * to configure filters.
     *
     * @param properties {@link Properties} containing information of filter path
     * @param prefix Prefix of the filter path in the {@link Properties}
     * @param filter {@link AuthenticationFilter} filter applied on the filter paths
     * @return collection with {@link Consumer} to be used with {@link FilterManager}
     */
    public static Set<Consumer<FilterManager>> filtersFrom(Properties properties, String prefix, Class<? extends AuthenticationFilter> filter)
    {
        Set<Consumer<FilterManager>> filters = new HashSet<>();

        for (Object key : properties.keySet())
            if (key.toString().startsWith(prefix))
                filters.add(pathToFilter(properties.getProperty(key.toString()), filter));

        return filters;
    }

    /**
     * Tool to generate a {@link Consumer} from a path. This {@link Consumer}
     * can be used with the {@link FilterManager} to configure filters.
     *
     * @param path path representing the path and the HTTP method applied on it (like PATH|METHOD|METHOD)
     * @param filter {@link AuthenticationFilter} filter applied on the filter path
     * @return {@link Consumer} to be used with {@link FilterManager}
     */
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

    /**
     * Tool to generate a collection of {@link Class} from {@link Properties}.
     * Used to load {@link Class} modules from property file
     *
     * @param properties {@link Properties} containing class names
     * @param prefix Prefix of the class names in the {@link Properties}
     * @return collection of {@link Class}
     */
    public static Set<Class<? extends AuthenticationModule>> modulesFrom(Properties properties, String prefix)
            throws IllegalArgumentException, LoadingModuleException, ClassNotFoundException
    {
        Set<Class<? extends AuthenticationModule>> moduleClazz = new HashSet<>();

        for (Object key : properties.keySet())
        {
            if (key.toString().startsWith(prefix))
            {
                final Class<? extends AuthenticationModule> clazz;
                clazz = loadClass(properties.getProperty(key.toString()), AuthenticationModule.class);
                moduleClazz.add(clazz);
            }
        }

        return moduleClazz;
    }


    /**
     * Generic tool to generate a {@link Class} from the class name.
     *
     * @param className Name of the class
     * @param originClazz {@link Class} of a parent of the class. Can be used to filter class
     * @return {@link Class} loaded thanks to its name
     */
    public static <T> Class<? extends T> loadClass(String className, Class<T> originClazz)
            throws IllegalArgumentException, LoadingModuleException, ClassNotFoundException
    {
        if (className == null || className.isEmpty())
            throw new IllegalArgumentException("Module name cannot be empty.");

        Class<?> clazz = originClazz.getClassLoader().loadClass(className);
        if (originClazz.isAssignableFrom(clazz))
            return (Class<? extends T>) clazz;

        throw new LoadingModuleException(className, String
                .format("Invalid class, must extend '%s'", originClazz.getName()));
    }

    /**
     * Tool used to split path in sub path.
     *
     * @param path Path to be splitted
     * @return {@link List} of all sub path
     */
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
}
