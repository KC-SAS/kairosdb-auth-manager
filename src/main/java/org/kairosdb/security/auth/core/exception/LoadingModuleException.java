package org.kairosdb.security.auth.core.exception;

public class LoadingModuleException extends Exception
{
    private final String moduleName;

    public LoadingModuleException(String moduleName, String message)
    {
        super(message);
        this.moduleName = moduleName;
    }

    public String getModuleName()
    {
        return moduleName;
    }
}
