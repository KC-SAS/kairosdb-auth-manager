package org.kairosdb.security.auth.core;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import org.kairosdb.security.auth.AuthenticationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.kairosdb.security.auth.core.Utils.modulesFrom;

public class AuthenticationManagerModule extends ServletModule
{
    public static final String KAIROSDB_SECURITY_PREFIX = "kairosdb.security.";
    private static final String AUTH_MODULES_PREFIX = KAIROSDB_SECURITY_PREFIX + "auth.modules.";
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerModule.class);
    private final Properties properties;

    public AuthenticationManagerModule(Properties properties)
    {
        this.properties = properties;
    }

    @Override
    protected void configureServlets()
    {
        bind(AuthenticationManagerFilter.class).in(Singleton.class);
        bind(FilterManager.class).in(Singleton.class);
        Multibinder<AuthenticationModule> modules = Multibinder.newSetBinder(binder(), AuthenticationModule.class);

        try
        {
            modulesFrom(properties, AUTH_MODULES_PREFIX).forEach(module -> modules.addBinding().to(module));
        } catch (Exception e)
        {
            logger.error(String.format("Failed to load modules from properties: %s", e.getMessage()), e);
        }

        filter("/*").through(AuthenticationManagerFilter.class);
    }
}
