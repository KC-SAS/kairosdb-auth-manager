package org.kairosdb.security.auth.core;

import org.kairosdb.security.auth.AuthenticationFilter;
import org.kairosdb.security.auth.AuthenticationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public class ModuleTools
{
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerModule.class);

    public static <T> Class<? extends T> loadModule(String moduleName, Class<T> originClazz)
    {
        if (moduleName == null || moduleName.isEmpty())
            return null;

        try
        {
            Class<?> clazz = originClazz.getClassLoader().loadClass(moduleName);
            if (originClazz.isAssignableFrom(clazz))
                return (Class<? extends T>) clazz;

            String failureMessage = String.format("Invalid class, must extend '%s'", originClazz.getName());
            logger.error(String.format("Unable to load module '%s': %s", moduleName, failureMessage));

        } catch (ClassNotFoundException e)
        {
            logger.error(String.format("Unable to load module '%s': %s", moduleName, "Class not found"), e);

        } catch (Exception e)
        {
            logger.error(String.format("Unable to load module '%s': %s", moduleName, e.getMessage()), e);
        }
        return null;
    }

    public static <T> T newInstance(Class<T> clazz)
    {
        if (clazz == null)
            return null;

        try
        {
            return clazz.newInstance();
        } catch (InstantiationException e)
        {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()))
                logger.error(String
                        .format("'%s' is an inner class but must be static to be instantiated.", clazz.getName()), e);
            else
                logger.error(String.format("'%s' must implement a parameterless ctor.", clazz.getName()), e);
        } catch (Exception ignore) {}

        return null;
    }

    public static Set<Class<? extends AuthenticationModule>> modulesFrom(Properties properties, String prefix)
    {
        Set<Class<? extends AuthenticationModule>> moduleClazz = new HashSet<>();

        for (Object key : properties.keySet())
        {
            if (key.toString().startsWith(prefix))
            {
                final Class<? extends AuthenticationModule> clazz = loadModule(properties
                        .getProperty(key.toString()), AuthenticationModule.class);
                if (clazz != null)
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

        if (items.length == 0)
            return (manager -> {});

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
