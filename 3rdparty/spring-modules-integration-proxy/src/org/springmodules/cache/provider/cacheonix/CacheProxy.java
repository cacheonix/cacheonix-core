/*
 * Created on July 12, 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright @2008 the original author or authors.
 * 
 * Additional note:
 * 
 * This class provides bridge between springmodules and cacheonix.
 * Please take additional care modifying this file, because it provides referential symbolic
 * links to cacheonix package via java reflection API.
 */
package org.springmodules.cache.provider.cacheonix;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CacheProxy
{
    // Reflection Data
    private static final String strCacheonixCache = "cacheonix.cache.Cache";
    
    private static final String strCacheGet = "get";
    private static final String strCachePut = "put";
    private static final String strCacheRemove = "remove";
    private static final String strCacheIsEmpty = "isEmpty";
    private static final String strCacheClear = "clear";
    private static final String strCacheShutdown = "shutdown";
    
    private static Method ccGet = null;
    private static Method ccPut = null;
    private static Method ccRemove = null;
    private static Method ccIsEmpty = null;
    private static Method ccClear = null;
    private static Method ccShutdown = null;
    
    // Initialization
    // Three state: 0 - not Initialized; 1 - initialized well; -1 - fail initialization
    private static int bProxyInitialized = 0;
    
    private static Class cacheonixCacheClass = checkAndGetCacheonixCache();

    private final Object cacheObject;
    
    private static synchronized Class checkAndGetCacheonixCache()
    {
        try
        {
            cacheonixCacheClass = Class.forName(strCacheonixCache);
            if (cacheonixCacheClass != null)
            {
                getRequiredMethods();
                bProxyInitialized = 1;
            }
            else
            {
                bProxyInitialized = -1;
            }
        }
        catch (Exception e)
        {
            bProxyInitialized = -1;
        } 
        
        return cacheonixCacheClass;
    }
    
    private static void getRequiredMethods()
    {
        try
        {
            Class[] args1 = new Class[]{Object.class};
            ccGet = cacheonixCacheClass.getDeclaredMethod(strCacheGet, args1);
            ccPut = cacheonixCacheClass.getDeclaredMethod(strCachePut, new Class[]{Object.class, Object.class});
            ccRemove = cacheonixCacheClass.getDeclaredMethod(strCacheRemove, args1);
            ccIsEmpty = cacheonixCacheClass.getDeclaredMethod(strCacheIsEmpty, null);
            ccClear = cacheonixCacheClass.getDeclaredMethod(strCacheClear, null);
            ccShutdown = cacheonixCacheClass.getDeclaredMethod(strCacheShutdown, null);
        }
        catch(Exception ex)
        {
            throw new IllegalStateException("Cannot find required methods in Cacheonix class library", ex);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    
    public CacheProxy(Object cache)
    {
        cacheObject = cache;
    }
    
    /////////////////////////////////////////////////////////////////////////
    
    public Object get(Object key)
    {
        Object res = null;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                Object[] args = new Object[]{key};
                res = ccGet.invoke(cacheObject, args);
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("get method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("get method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("get method got invocation problem. Check nested Exceptions", e);
        }
        
        return res;
    }

    public Object put(Object key, Object val)
    {
        Object res = null;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                Object[] args = new Object[]{key, val};
                res = ccPut.invoke(cacheObject, args);
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("put method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("put method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("put method got invocation problem. Check nested Exceptions", e);
        }
        
        return res;
    }

    public Object remove(Object key)
    {
        Object res = null;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                Object[] args = new Object[]{key};
                res = ccRemove.invoke(cacheObject, args);
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("remove method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("remove method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("remove method got invocation problem. Check nested Exceptions", e);
        }
        
        return res;
    }

    public boolean isEmpty()
    {
        boolean res = true;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                res = ((Boolean)ccIsEmpty.invoke(cacheObject, null)).booleanValue();
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("isEmpty method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("isEmpty method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("isEmpty method got invocation problem. Check nested Exceptions", e);
        }
        
        return res;
    }

    public void clear()
    {
        try
        {
            if (bProxyInitialized == 1)
            {
                ccClear.invoke(cacheObject, null);
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("clear method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("clear method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("clear method got invocation problem. Check nested Exceptions", e);
        }
    }

    public void shutdown()
    {
        try
        {
            if (bProxyInitialized == 1)
            {
                ccShutdown.invoke(cacheObject, null);
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'");
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("shutdown method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("shutdown method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("shutdown method got invocation problem. Check nested Exceptions", e);
        }
    }
}
