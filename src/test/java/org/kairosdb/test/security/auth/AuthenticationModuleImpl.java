package org.kairosdb.test.security.auth;

import org.kairosdb.security.auth.AuthenticationModule;
import org.kairosdb.security.auth.core.FilterManager;

import java.util.Properties;

import static org.kairosdb.security.auth.core.AuthenticationManagerModule.KAIROSDB_SECURITY_PREFIX;
import static org.kairosdb.security.auth.core.Utils.filtersFrom;

public class AuthenticationModuleImpl
{
    public static class FromProperties implements AuthenticationModule
    {
        public boolean isConfigured = false;

        @Override
        public void configure(Properties properties, FilterManager manager)
        {
            isConfigured = true;
            if (properties == null)
                return;

            filtersFrom(properties, KAIROSDB_SECURITY_PREFIX + "auth.test.allowed_path.", AuthenticationFilterImpl.AllowFilter.class)
                    .forEach(f -> f.accept(manager));
            filtersFrom(properties, KAIROSDB_SECURITY_PREFIX + "auth.test.denied_path.", AuthenticationFilterImpl.DenyFilter.class)
                    .forEach(f -> f.accept(manager));
        }
    }

    public static class FromCode implements AuthenticationModule
    {
        @Override
        public void configure(Properties properties, FilterManager manager)
        {
            manager.filter("/test/allow")
                    .from("GET").from("PATCH")
                    .through(AuthenticationFilterImpl.AllowFilter.class);
            manager.filter("/test/allow")
                    .from("POST").from("OPTION")
                    .through(AuthenticationFilterImpl.DenyFilter.class);

            manager.filter("/test/deny")
                    .from("POST")
                    .through(AuthenticationFilterImpl.DenyFilter.class);
        }
    }
}
