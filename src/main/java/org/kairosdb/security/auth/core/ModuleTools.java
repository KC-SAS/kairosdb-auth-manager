package org.kairosdb.security.auth.core;

import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.LoadingModuleException;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public class ModuleTools
{
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

    public static <T> T newInstance(Class<T> clazz)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException
    {
        if (clazz == null)
            throw new IllegalArgumentException("Class parameter cannot be null.");

        try
        {
            return clazz.newInstance();
        } catch (InstantiationException e)
        {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()))
            {
                final String format = "'%s' is an inner class but must be static to be instantiated.";
                throw new InstantiationException(String.format(format, clazz.getName()));
            } else
            {
                final String format = "'%s' must implement a parameterless ctor.";
                throw new InstantiationException(String.format(format, clazz.getName()));
            }
        }
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

    public static Set<Consumer<FilterManager>> filterFrom(Properties properties, String prefix, Class<? extends AuthenticationFilter> filter)
    {
        Set<Consumer<FilterManager>> filters = new HashSet<>();

        for (Object key : properties.keySet())
        {
            if (key.toString().startsWith(prefix))
                filters.add(pathToFilter(properties.getProperty(key.toString()), filter));
        }

        return filters;
    }

    public static Consumer<FilterManager> pathToFilter(String path, Class<? extends AuthenticationFilter> filter)
    {
        String[] items = path.split("\\|");

        if (items.length == 1 && items[0].isEmpty())
            return (manager ->
            {
            });

        return (manager ->
        {
            FilterManager.FilterBuilder builder = manager.filter(items[0].trim());

            if (items.length < 2)
                builder.from("Get");
            for (int i = 1; i < items.length; i++)
                if (!items[i].isEmpty())
                    builder.from(items[i].trim().toUpperCase());

            builder.through(filter);
        });
    }
}
