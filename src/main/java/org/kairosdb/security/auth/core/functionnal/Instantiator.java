package org.kairosdb.security.auth.core.functionnal;


import com.google.inject.ConfigurationException;
import com.google.inject.ProvisionException;

@FunctionalInterface
public interface Instantiator
{
    <T> T apply(Class<? extends T> clazz) throws ConfigurationException, ProvisionException;
}
