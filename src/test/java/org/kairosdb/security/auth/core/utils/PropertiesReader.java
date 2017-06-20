package org.kairosdb.security.auth.core.utils;

import java.io.IOException;
import java.io.InputStream;

public class PropertiesReader
{
    public static java.util.Properties getProperties()
    {
        java.util.Properties properties = new java.util.Properties();
        try
        {
            InputStream in = ClassLoader.getSystemResourceAsStream("auth.properties");
            properties.load(in);
            in.close();
        } catch (IOException ignore) {}

        return properties;
    }
}
