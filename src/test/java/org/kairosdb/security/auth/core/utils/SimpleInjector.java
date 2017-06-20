package org.kairosdb.security.auth.core.utils;

import com.google.inject.*;
import com.google.inject.spi.TypeConverterBinding;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleInjector implements com.google.inject.Injector
{
    private ExceptionThrower thrower = null;

    @Override
    public <T> T getInstance(Class<T> aClass)
    {
        try
        {
            return aClass.newInstance();
        } catch (Exception e)
        {
            throw_(e);
        }
        return null;
    }

    public void throwIfFailure() throws Exception
    {
        if (thrower != null)
            thrower.apply();
    }

    private void throw_(Exception e)
    {
        thrower = () ->
        {
            if (e != null) throw e;
        };
    }


    @FunctionalInterface
    public interface ExceptionThrower
    {
        void apply() throws Exception;
    }

    //region Injector not implemented methods
    @Override
    public void injectMembers(Object o)
    {

    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral)
    {
        return null;
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> aClass)
    {
        return null;
    }

    @Override
    public Map<Key<?>, Binding<?>> getBindings()
    {
        return null;
    }

    @Override
    public Map<Key<?>, Binding<?>> getAllBindings()
    {
        return null;
    }

    @Override
    public <T> Binding<T> getBinding(Key<T> key)
    {
        return null;
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> aClass)
    {
        return null;
    }

    @Override
    public <T> Binding<T> getExistingBinding(Key<T> key)
    {
        return null;
    }

    @Override
    public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> typeLiteral)
    {
        return null;
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key)
    {
        return null;
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> aClass)
    {
        return null;
    }

    @Override
    public <T> T getInstance(Key<T> key)
    {
        return null;
    }

    @Override
    public Injector getParent()
    {
        return null;
    }

    @Override
    public Injector createChildInjector(Iterable<? extends Module> iterable)
    {
        return null;
    }

    @Override
    public Injector createChildInjector(Module... modules)
    {
        return null;
    }

    @Override
    public Map<Class<? extends Annotation>, Scope> getScopeBindings()
    {
        return null;
    }

    @Override
    public Set<TypeConverterBinding> getTypeConverterBindings()
    {
        return null;
    }
    //endregion
}
