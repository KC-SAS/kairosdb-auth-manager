package org.kairosdb.security.auth;

import org.kairosdb.security.auth.core.FilterManager;

import java.util.Properties;

public interface AuthenticationModule
{
    void configure(Properties properties, FilterManager manager);
}
