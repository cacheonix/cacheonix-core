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

public final class CacheManagerProxy
{
    // Reflection Data
    private static final String strCacheonixCacheManager = "cacheonix.cache.CacheManager";
    private static final String strGetInstance = "getInstance";
    private static final String strGetCache = "getCache";
    private static final String strShutdown = "shutdown";
    
    private static Method cmGetInstance = null;
    private static Method cmGetInstanceNargs = null;
    private static Method cmGetCache = null;
    private static Method cmShutdown = null;
    
    // Initialization
    // Three state: 0 - not Initialized; 1 - initialized well; -1 - fail initialization
    private static int bProxyInitialized = 0;
    
    private static Class cacheonixCacheManagerClass = checkAndGetCacheonixCacheManager();
    
    public static Object getInstance()
    {
        Object resInstance = null;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                resInstance = cmGetInstance.invoke(null, null);
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
            throw new IllegalStateException("getInstsnce method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("getInstsnce method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("getInstsnce method got invocation problem. Check nested Exceptions", e);
        }
        
        return resInstance;
    }

    public static Object getInstance(String cacheConfigName)
    {
        Object resInstance = null;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                Object[] args = new Object[]{cacheConfigName};
                resInstance = cmGetInstanceNargs.invoke(null, args);
            }
            else if (bProxyInitialized == -1)
            {
                throw new IllegalStateException("Cacheonix library cannot be used! It has initialization error. State is '" + bProxyInitialized + "'");
            }
            else
            {
                throw new IllegalStateException("Cacheonix library was not Initialized yet! State is '" + bProxyInitialized + "'" );
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("getInstsnce method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("getInstsnce method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("getInstsnce method got invocation problem. Check nested Exceptions", e);
        }
        
        return resInstance;
    }

    public static Object getCache(Object cacheManager, String cacheName)
    {
        Object resCache = null;
        
        try
        {
            if (bProxyInitialized == 1)
            {
                Object[] args = new Object[]{new String(cacheName)};
                resCache = cmGetCache.invoke(cacheManager, args);
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
            throw new IllegalStateException("getCache method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("getCache method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("getCache method got invocation problem. Check nested Exceptions", e);
        }
        
        return resCache;
    }

    public static void shutdowmCacheManager(Object cacheManager)
    {
        try
        {
            if (bProxyInitialized == 1)
            {
                cmShutdown.invoke(cacheManager, null);
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
            throw new IllegalStateException("getCache method has wrong arguments. Cacheonix class library wrong version! Check nested Exceptions", e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("getCache method violate security settings. Check nested Exceptions", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("getCache method got invocation problem. Check nested Exceptions", e);
        }
    }

    private static synchronized Class checkAndGetCacheonixCacheManager()
    {
        try
        {
            cacheonixCacheManagerClass = Class.forName(strCacheonixCacheManager);
            if (cacheonixCacheManagerClass != null)
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
        
        return cacheonixCacheManagerClass;
    }
    
    public static boolean isCacheonixApiFound()
    {
        return (bProxyInitialized == 1);
    }
    
    private static void getRequiredMethods()
    {
        try
        {
            Class[] args = new Class[]{String.class};
            cmGetInstance = cacheonixCacheManagerClass.getDeclaredMethod(strGetInstance, null);
            cmGetInstanceNargs = cacheonixCacheManagerClass.getDeclaredMethod(strGetInstance, args);
            cmGetCache = cacheonixCacheManagerClass.getDeclaredMethod(strGetCache, args);
            cmShutdown = cacheonixCacheManagerClass.getDeclaredMethod(strShutdown, null);
        }
        catch(Exception ex)
        {
            throw new IllegalStateException("Cannot find required methods in Cacheonix class library", ex);
        }
    }
}
