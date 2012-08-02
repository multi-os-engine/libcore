/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.security.fortress;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * This class contains information about all registered providers and preferred
 * implementations for all "serviceName.algName".
 */
public class Services {

    /**
     * The HashMap that contains information about preferred implementations for
     * all serviceName.algName in the registered providers.
     * Set the initial size to 600 so we don't grow to 1024 by default because
     * initialization adds a few entries more than the growth threshold.
     */
    private static final Map<String, Provider.Service> services
            = new HashMap<String, Provider.Service>(600);

    /**
     * Save default SecureRandom service as well.
     * Avoids similar provider/services iteration in SecureRandom constructor.
     */
    private static Provider.Service cachedSecureRandomService;

    /**
     * Need refresh flag. Protected by synchronizing on providers.
     */
    private static boolean needRefresh;

    /**
     * The refreshNumber is changed on every update of servince
     * information. It is used by external caller to validate their
     * own caches of Service information.
     */
    static volatile int refreshNumber = 1;

    /**
     * Registered providers
     */
    private static final List<Provider> providers = new ArrayList<Provider>(20);

    /**
     * Hash for quick provider access by name. Protected by synchronizing on providers.
     */
    private static final Map<String, Provider> providersNames = new HashMap<String, Provider>(20);
    static {
        loadProviders();
    }

    private static void loadProviders() {
        synchronized (providers) {
            String providerClassName = null;
            int i = 1;
            ClassLoader cl = ClassLoader.getSystemClassLoader();

            while ((providerClassName = Security.getProperty("security.provider." + i++)) != null) {
                try {
                    Class providerClass = Class.forName(providerClassName.trim(), true, cl);
                    Provider p = (Provider) providerClass.newInstance();
                    providers.add(p);
                    providersNames.put(p.getName(), p);
                    initServiceInfo(p);
                } catch (ClassNotFoundException ignored) {
                } catch (IllegalAccessException ignored) {
                } catch (InstantiationException ignored) {
                }
            }
            Engine.door.renumProviders();
        }
    }

    /**
     * Returns a copy of the registered providers as an array.
     */
    public static Provider[] getProviders() {
        synchronized (providers) {
            return providers.toArray(new Provider[providers.size()]);
        }
    }

    /**
     * Returns a copy of the registered providers as a List.
     */
    public static List<Provider> getProvidersList() {
        synchronized (providers) {
            return new ArrayList<Provider>(providers);
        }
    }

    /**
     * Returns the provider with the specified name.
     */
    public static Provider getProvider(String name) {
        if (name == null) {
            return null;
        }
        synchronized (providers) {
            return providersNames.get(name);
        }
    }

    /**
     * Inserts a provider at a specified 1-based position.
     */
    public static int insertProviderAt(Provider provider, int position) {
        synchronized (providers) {
            int size = providers.size();
            if ((position < 1) || (position > size)) {
                position = size + 1;
            }
            providers.add(position - 1, provider);
            providersNames.put(provider.getName(), provider);
            setNeedRefresh();
            return position;
        }
    }

    /**
     * Removes the provider at the specified 1-based position.
     */
    public static void removeProvider(int providerNumber) {
        synchronized (providers) {
            Provider p = providers.remove(providerNumber - 1);
            providersNames.remove(p.getName());
            setNeedRefresh();
        }
    }

    /**
     * Adds information about provider services into HashMap.
     */
    public static void initServiceInfo(Provider p) {
        synchronized (services) {
            for (Provider.Service service : p.getServices()) {
                String type = service.getType();
                if (cachedSecureRandomService == null && type.equals("SecureRandom")) {
                    cachedSecureRandomService = service;
                }
                String key = type + "." + service.getAlgorithm().toUpperCase(Locale.US);
                if (!services.containsKey(key)) {
                    services.put(key, service);
                }
                for (String alias : Engine.door.getAliases(service)) {
                    key = type + "." + alias.toUpperCase(Locale.US);
                    if (!services.containsKey(key)) {
                        services.put(key, service);
                    }
                }
            }
        }
    }

    /**
     * Returns true if services contain any provider information.
     */
    public static boolean isEmpty() {
        synchronized (services) {
            return services.isEmpty();
        }
    }

    /**
     * Returns service description for the TYPE.ALGORITHM format key.
     *
     * Typically the caller should call
     * flushCachesAndRefreshServicesIfNeeded() and validate any of
     * their own caches against the refreshNumber before resorting to
     * calling this method.
     */
    public static Provider.Service getService(String key) {
        synchronized (services) {
            return services.get(key);
        }
    }

    /**
     * Returns the default SecureRandom service description.
     */
    public static Provider.Service getSecureRandomService() {
        flushCachesAndRefreshServicesIfNeeded();
        return cachedSecureRandomService;
    }

    /**
     * In addition to being used here when the list of providers
     * changes, it is also used by the Provider implementation to
     * indicate that a provides list of services has changed.
     */
    public static void setNeedRefresh() {
        synchronized (providers) {
            needRefresh = true;
        }
    }

    public static void flushCachesAndRefreshServicesIfNeeded() {
        synchronized (providers) {
            if (needRefresh) {
                refreshNumber++;
                synchronized (services) {
                    services.clear();
                }
                cachedSecureRandomService = null;
                for (Provider p : providers) {
                    initServiceInfo(p);
                }
                needRefresh = false;
            }
        }
    }
}
