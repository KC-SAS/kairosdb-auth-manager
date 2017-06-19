package org.kairosdb.security.auth.core.utils;

import org.kairosdb.security.auth.AuthenticationModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class AuthTestTools
{
    public static Properties getAuthProperties()
    {
        Properties properties = new Properties();
        try
        {
            InputStream in = ClassLoader.getSystemResourceAsStream("auth.properties");
            properties.load(in);
            in.close();
        } catch (IOException ignore) {}

        return properties;
    }

    public static Set<AuthenticationModule> getAuthModule()
    {
        return null;
    }
}
