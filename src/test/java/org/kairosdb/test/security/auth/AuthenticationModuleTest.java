package org.kairosdb.test.security.auth;

import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.FilterManager;

import java.util.Properties;

import static org.kairosdb.security.auth.core.AuthenticationManagerModule.KAIROSDB_SECURITY_PREFIX;
import static org.kairosdb.security.auth.core.ModuleTools.filterFrom;

public class AuthenticationModuleTest implements AuthenticationModule
{
    public boolean isConfigured = false;

    @Override
    public void configure(Properties properties, FilterManager manager)
    {
        isConfigured = true;
        if (properties == null)
            return;

        filterFrom(properties, KAIROSDB_SECURITY_PREFIX + "auth.test.path.", AuthenticationFilterTest.class)
                .forEach(f -> f.accept(manager));
    }
}
