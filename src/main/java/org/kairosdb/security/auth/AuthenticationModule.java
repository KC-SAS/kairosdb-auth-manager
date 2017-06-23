package org.kairosdb.security.auth;

import org.kairosdb.security.auth.core.FilterManager;

import java.util.Properties;

/**
 * Interface for an authentication module
 */
public interface AuthenticationModule
{
    /**
     * Entry point of the module, it allows the filters the
     * configuration of the module filters.
     *
     * @param properties {@link Properties} including information for the configuration (path, password, ...)
     * @param manager {@link FilterManager} allowing the filters configuration
     */
    void configure(Properties properties, FilterManager manager);
}
