package org.kairosdb.security.auth.core;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.exception.LoadingModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.kairosdb.security.auth.core.ModuleTools.modulesFrom;

public class AuthenticationManagerModule extends ServletModule
{
    public static final String KAIROSDB_SECURITY_PREFIX = "kairosdb.security.";
    private static final String AUTH_MODULES_PREFIX = KAIROSDB_SECURITY_PREFIX + "auth.modules.";
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerModule.class);

    private final Properties properties;
    private final Multibinder<AuthenticationModule> authenticationModules;


    public AuthenticationManagerModule(Properties properties)
    {
        this.properties = properties;
        this.authenticationModules = Multibinder.newSetBinder(binder(), AuthenticationModule.class);
    }

    @Override
    protected void configureServlets()
    {
        bind(AuthenticationManagerFilter.class).in(Scopes.SINGLETON);
        bind(FilterManager.class).in(Scopes.SINGLETON);

        try
        {
            modulesFrom(properties, AUTH_MODULES_PREFIX).forEach(this::bindModule);
        } catch (LoadingModuleException | ClassNotFoundException e)
        {
            logger.error(String.format("Failed to load modules from properties: %s", e.getMessage()), e);
        }
    }

    private void bindModule(Class<? extends AuthenticationModule> clazz)
    {
        authenticationModules.addBinding().to(clazz);
    }
}
